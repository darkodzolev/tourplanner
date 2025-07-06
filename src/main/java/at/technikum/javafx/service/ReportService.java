package at.technikum.javafx.service;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Arrays;

import javafx.application.Platform;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportService implements IReportService {
    private final ITourLogService tourLogService;
    private final Path leafletDir = Paths.get(
            System.getProperty("user.home"), ".tourplanner", "leaflet"
    );

    public ReportService(ITourLogService tourLogService) {
        this.tourLogService = tourLogService;
    }

    public void generateTourReport(Tour tour, WebView ignoredMapView, File outputPdf) throws Exception {
        // 1) compile the JRXML
        try (InputStream jrStream = getClass().getResourceAsStream("/reports/tour-report.jrxml")) {
            JasperReport jasperReport = JasperCompileManager.compileReport(jrStream);

            // 2) fetch logs & build parameters
            List<TourLog> logs = tourLogService.getLogsForTour(tour);
            Map<String,Object> params = new HashMap<>();
            params.put("tourName",        tour.getName());
            params.put("tourDescription", tour.getDescription());
            params.put("fromLocation",    tour.getFromLocation());
            params.put("toLocation",      tour.getToLocation());
            params.put("transportType",   tour.getTransportType());
            params.put("distance",        tour.getDistance());
            params.put("estimatedTime",   tour.getEstimatedTime());
            params.put("popularity",      String.valueOf(logs.size()));
            params.put("childFriendly",   String.format("%.2f", computeChildFriendliness(logs)));

            WebEngine engine = ignoredMapView.getEngine();
            if (engine.getLoadWorker().getState() != Worker.State.SUCCEEDED) {
                CountDownLatch loadLatch = new CountDownLatch(1);
                engine.getLoadWorker().stateProperty().addListener((obs,oldState,newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        loadLatch.countDown();
                    }
                });
                if (!loadLatch.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Map did not finish loading");
                }
            }

            WritableImage snapshotImage;

            if (Platform.isFxApplicationThread()) {
                snapshotImage = ignoredMapView.snapshot(new SnapshotParameters(), null);
            } else {
                // otherwise queue it and wait up to 5s
                CountDownLatch snapLatch = new CountDownLatch(1);
                final WritableImage[] tmp = new WritableImage[1];
                Platform.runLater(() -> {
                    tmp[0] = ignoredMapView.snapshot(new SnapshotParameters(), null);
                    snapLatch.countDown();
                });
                if (!snapLatch.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Snapshot timed out");
                }
                snapshotImage = tmp[0];
            }
            File mapPng = File.createTempFile("tour-map-", ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(snapshotImage, null), "png", mapPng);
            params.put("mapImage", mapPng.getAbsolutePath());

            // 4) fill report & export PDF
            JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(logs);
            JasperPrint jp = JasperFillManager.fillReport(jasperReport, params, ds);
            JasperExportManager.exportReportToPdfFile(jp, outputPdf.getAbsolutePath());
        }
    }

    public void generateSummaryReport(List<Tour> allTours, File outputPdf) throws Exception {
        try (InputStream jrStream = getClass().getResourceAsStream("/reports/summary-report.jrxml")) {
            JasperReport jasperReport = JasperCompileManager.compileReport(jrStream);

            // Build summary beans
            List<TourSummary> summaries = allTours.stream().map(t -> {
                List<TourLog> logs = tourLogService.getLogsForTour(t);
                double avgDist = logs.stream().mapToDouble(TourLog::getTotalDistance).average().orElse(0);
                double avgRating = logs.stream().mapToDouble(TourLog::getRating).average().orElse(0);
                long avgSecs = (long) logs.stream()
                        .mapToLong(l -> safeParseSeconds(l.getTotalTime()))
                        .average().orElse(0);
                String avgTime = formatDuration(avgSecs);
                return new TourSummary(t.getName(), avgDist, avgTime, avgRating);
            }).collect(Collectors.toList());

            //  Empty params for summary
            Map<String, Object> params = new HashMap<>();

            // Fill and export
            JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(summaries);
            JasperPrint jp = JasperFillManager.fillReport(jasperReport, params, ds);
            JasperExportManager.exportReportToPdfFile(jp, outputPdf.getAbsolutePath());
        }
    }

    private double computeChildFriendliness(List<TourLog> logs) {
        if (logs.isEmpty()) return 0.0;
        double[] diffs = logs.stream()
                .map(TourLog::getDifficulty)
                .flatMap(s -> {
                    try { double d = Double.parseDouble(s);
                        return (d >= 1 && d <= 5) ? Stream.of(d) : Stream.empty();
                    } catch (NumberFormatException ex) { return Stream.empty(); }
                })
                .mapToDouble(d -> d)
                .toArray();
        double diffScore = (diffs.length == 0)
                ? 0.5
                : (5.0 - Arrays.stream(diffs).average().getAsDouble()) / 4.0;

        double[] times = logs.stream()
                .mapToLong(l -> safeParseSeconds(l.getTotalTime()))
                .filter(t -> t >= 0)
                .mapToDouble(t -> t)
                .toArray();
        double timeScore = (times.length == 0)
                ? 0.5
                : 1.0 / (Arrays.stream(times).average().getAsDouble() / 3600.0 + 1.0);

        double[] dists = logs.stream()
                .mapToDouble(TourLog::getTotalDistance)
                .filter(d -> d >= 0)
                .toArray();
        double distScore = (dists.length == 0)
                ? 0.5
                : 1.0 / (Arrays.stream(dists).average().getAsDouble() / 1000.0 + 1.0);

        double cf = (diffScore + timeScore + distScore) / 3.0;
        return Math.max(0.0, Math.min(cf, 1.0));
    }

    private long safeParseSeconds(String hms) {
        if (hms == null) return 0;
        String[] parts = hms.split(":");
        if (parts.length != 3) return 0;
        try {
            long h = Long.parseLong(parts[0]);
            long m = Long.parseLong(parts[1]);
            long s = Long.parseLong(parts[2]);
            return h*3600 + m*60 + s;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatDuration(long secs) {
        Duration d = Duration.ofSeconds(secs);
        long hours = d.toHours();
        long minutes = d.minusHours(hours).toMinutes();
        return (hours > 0)
                ? String.format("%d h %d m", hours, minutes)
                : String.format("%d m", minutes);
    }
}