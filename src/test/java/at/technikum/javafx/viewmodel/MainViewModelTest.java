package at.technikum.javafx.viewmodel;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MainViewModelTest {

    private MainViewModel vm;

    @BeforeEach
    void setUp() {
        vm = new MainViewModel(null);
    }

    @Test
    void darkMode_defaultIsFalse() {
        assertFalse(vm.darkModeProperty().get(), "darkMode should default to false");
    }

    @Test
    void setDarkMode_togglesProperty() {
        vm.setDarkMode(true);
        assertTrue(vm.darkModeProperty().get());
        vm.setDarkMode(false);
        assertFalse(vm.darkModeProperty().get());
    }
}