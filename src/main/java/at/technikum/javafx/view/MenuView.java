package at.technikum.javafx.view;

import at.technikum.javafx.viewmodel.MenuViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuView implements Initializable {

    private final MenuViewModel viewModel;

    public MenuView(MenuViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    public void onHistoryClear() {
        viewModel.clearHistory();
    }
}
