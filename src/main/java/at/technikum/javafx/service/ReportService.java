package at.technikum.javafx.service;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class ReportService implements IReportService {
    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ITourLogService tourLogService;
    private final Path leafletDir = Paths.get(
            System.getProperty("user.home"), ".tourplanner", "leaflet"
    );

    public ReportService(ITourLogService tourLogService) {
        this.tourLogService = tourLogService;
        log.info("ReportService initialized; leafletDir={}", leafletDir);
    }

    @Override
    public void generateTourReport(Tour tour, WebView ignoredMapView, File outputPdf) throws Exception {
        log.info("generateTourReport: tourId={} output='{}'", tour.getId(), outputPdf.getAbsolutePath());
        long startMs = System.currentTimeMillis();

        try (InputStream jrStream = getClass().getResourceAsStream("/reports/tour-report.jrxml")) {
            JasperReport jasperReport = JasperCompileManager.compileReport(jrStream);

            List<TourLog> logs = tourLogService.getLogsForTour(tour);

            double rawMeters = tour.getDistance();
            double km = Math.round((rawMeters / 1000.0) * 10.0) / 10.0;

            // Fill parameters for JasperReport
            Map<String, Object> params = new HashMap<>();
            params.put("tourName",        tour.getName());
            params.put("tourDescription", tour.getDescription());
            params.put("fromLocation",    tour.getFromLocation());
            params.put("toLocation",      tour.getToLocation());
            params.put("transportType",   tour.getTransportType());
            params.put("distance",        km);
            params.put("estimatedTime",   tour.getEstimatedTime());
            params.put("popularity",      String.valueOf(logs.size()));
            params.put("childFriendly",   String.format("%.2f", computeChildFriendliness(logs)));

            // Ensure map is fully loaded before snapshot
            WebEngine engine = ignoredMapView.getEngine();
            if (engine.getLoadWorker().getState() != Worker.State.SUCCEEDED) {
                CountDownLatch loadLatch = new CountDownLatch(1);
                engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) loadLatch.countDown();
                });
                if (!loadLatch.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Map did not finish loading");
                }
            }

            // Take snapshot of the map for the report
            WritableImage snapshotImage;
            if (Platform.isFxApplicationThread()) {
                snapshotImage = ignoredMapView.snapshot(new SnapshotParameters(), null);
            } else {
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

            // Save map image temporarily for report
            File mapPng = File.createTempFile("tour-map-", ".png");
            ImageIO.write(SwingFXUtils.fromFXImage(snapshotImage, null), "png", mapPng);
            params.put("mapImage", mapPng.getAbsolutePath());

            // Fill and export the PDF
            JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(logs);
            JasperPrint jp = JasperFillManager.fillReport(jasperReport, params, ds);
            JasperExportManager.exportReportToPdfFile(jp, outputPdf.getAbsolutePath());

            log.info("Finished generateTourReport for tourId={} -> '{}' ({} ms)",
                    tour.getId(), outputPdf.getAbsolutePath(), System.currentTimeMillis() - startMs);
        } catch (Exception e) {
            log.error("Error in generateTourReport for tourId={} output='{}'",
                    tour.getId(), outputPdf.getAbsolutePath(), e);
            throw e;
        }
    }

    @Override
    public void generateSummaryReport(List<Tour> allTours, File outputPdf) throws Exception {
        log.info("generateSummaryReport: toursCount={} output='{}'", allTours.size(), outputPdf.getAbsolutePath());
        long startMs = System.currentTimeMillis();

        try (InputStream jrStream = getClass().getResourceAsStream("/reports/summary-report.jrxml")) {
            JasperReport jasperReport = JasperCompileManager.compileReport(jrStream);

            // Build summary beans (aggregated data for each tour)
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

            JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(summaries);
            Map<String, Object> params = new HashMap<>();
            JasperPrint jp = JasperFillManager.fillReport(jasperReport, params, ds);
            JasperExportManager.exportReportToPdfFile(jp, outputPdf.getAbsolutePath());

            log.info("Finished generateSummaryReport -> '{}' ({} ms)",
                    outputPdf.getAbsolutePath(), System.currentTimeMillis() - startMs);
        } catch (Exception e) {
            log.error("Error in generateSummaryReport output='{}'", outputPdf.getAbsolutePath(), e);
            throw e;
        }
    }

    // Simple metric based on difficulty, time, and distance
    private double computeChildFriendliness(List<TourLog> logs) {
        if (logs.isEmpty()) return 0.0;

        double[] diffs = logs.stream()
                .map(TourLog::getDifficulty)
                .flatMap(s -> {
                    try {
                        double d = Double.parseDouble(s);
                        return (d >= 1 && d <= 5) ? Stream.of(d) : Stream.empty();
                    } catch (NumberFormatException ex) {
                        return Stream.empty();
                    }
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

    // Safely parse time strings like "01:30:45" into seconds
    private long safeParseSeconds(String hms) {
        if (hms == null) return 0;
        String[] parts = hms.split(":");
        if (parts.length != 3) return 0;
        try {
            long h = Long.parseLong(parts[0]);
            long m = Long.parseLong(parts[1]);
            long s = Long.parseLong(parts[2]);
            return h * 3600 + m * 60 + s;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Convert seconds to formatted duration like "1 h 15 m"
    private String formatDuration(long secs) {
        Duration d = Duration.ofSeconds(secs);
        long hours = d.toHours();
        long minutes = d.minusHours(hours).toMinutes();
        return (hours > 0)
                ? String.format("%d h %d m", hours, minutes)
                : String.format("%d m", minutes);
    }
}