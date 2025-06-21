package at.technikum.javafx.view;

import at.technikum.javafx.viewmodel.SearchViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SearchView implements Initializable {

    private final SearchViewModel viewModel;

    @FXML
    private TextField searchInput;

    @FXML
    private Button searchButton;

    public SearchView(SearchViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchInput.textProperty()
                .bindBidirectional(viewModel.searchTextProperty());
        searchButton.disableProperty()
                .bind(viewModel.searchDisabledProperty());
    }

    @FXML
    public void onSearch() {
        viewModel.search();
    }
}
