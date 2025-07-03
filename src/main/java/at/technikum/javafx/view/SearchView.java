package at.technikum.javafx.view;

import at.technikum.javafx.viewmodel.SearchViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

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
        // bidirectional binding so every keystroke fires the SEARCH_TERM_SELECTED event
        searchInput.textProperty().bindBidirectional(viewModel.searchTextProperty());
    }
}