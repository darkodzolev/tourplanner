package at.technikum.javafx.service;

import java.io.File;
import javafx.scene.web.WebView;
import at.technikum.javafx.entity.Tour;
import java.util.List;

public interface IReportService {
    void generateTourReport(
            Tour tour,
            WebView ignoredMapView,
            File outputPdf
    ) throws Exception;

    void generateSummaryReport(List<Tour> allTours, File outputPdf) throws Exception;
}