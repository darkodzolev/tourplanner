package at.technikum.javafx.view;

import at.technikum.javafx.viewmodel.SearchViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;

public class SearchView implements Initializable {

    private final SearchViewModel viewModel;

    @FXML
    private TextField searchInput;

    public SearchView(SearchViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Bidirectional binding triggers search updates on keystroke
            searchInput.textProperty().bindBidirectional(viewModel.searchTextProperty());
        } catch (Exception ex) {
            showException("Search view error", ex);
        }
    }

    private void showException(String title, Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(ex.getMessage() != null ? ex.getMessage() : title);

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        alert.getDialogPane().setExpandableContent(new TitledPane("Details", textArea));
        alert.showAndWait();
    }
}