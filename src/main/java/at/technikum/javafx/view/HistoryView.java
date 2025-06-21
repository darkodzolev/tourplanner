package at.technikum.javafx.view;

import at.technikum.javafx.viewmodel.HistoryViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

public class HistoryView implements Initializable {

    private final HistoryViewModel viewModel;

    @FXML
    private ListView<String> searchHistoryList;

    public HistoryView(HistoryViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchHistoryList.setItems(viewModel.getSearchHistory());
        viewModel.selectedItemProperty()
                .bind(searchHistoryList.getSelectionModel().selectedItemProperty());
    }
}
