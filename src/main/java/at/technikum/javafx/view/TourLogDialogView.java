package at.technikum.javafx.view;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.viewmodel.TourLogDialogViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class TourLogDialogView implements Initializable {

    @FXML private TextArea commentField;
    @FXML private ComboBox<String> difficultyCombo;
    @FXML private TextField distanceField;
    @FXML private TextField timeField;
    @FXML private Spinner<Integer> ratingSpinner;

    private final TourLogDialogViewModel vm = new TourLogDialogViewModel();
    private Tour parentTour;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            difficultyCombo.setItems(vm.getDifficultyOptions());
            ratingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1));
            ratingSpinner.setEditable(true);

            commentField.textProperty().bindBidirectional(vm.commentProperty());
            difficultyCombo.valueProperty().bindBidirectional(vm.difficultyProperty());
            distanceField.textProperty().bindBidirectional(vm.distanceProperty());
            timeField.textProperty().bindBidirectional(vm.timeProperty());
            ratingSpinner.getValueFactory().valueProperty().bindBidirectional(vm.ratingProperty().asObject());

            // Custom formatter for time input (hh:mm:ss)
            UnaryOperator<TextFormatter.Change> filter = change -> {
                String oldText = change.getControlText();
                String newText = change.getControlNewText();
                String digits = newText.replaceAll("\\D", "");
                if (digits.length() > 6) digits = digits.substring(0, 6);

                StringBuilder formatted = new StringBuilder();
                int caretInDigits = change.getCaretPosition() - countNonDigits(newText, change.getCaretPosition());
                caretInDigits = Math.min(caretInDigits, digits.length());
                int newCaretPos = 0;

                for (int i = 0, d = 0; d < digits.length(); ++i) {
                    if (i == 2 || i == 5) {
                        formatted.append(':');
                    } else {
                        formatted.append(digits.charAt(d++));
                    }
                    if (d == caretInDigits) newCaretPos = formatted.length();
                }

                change.setRange(0, oldText.length());
                change.setText(formatted.toString());
                change.selectRange(newCaretPos, newCaretPos);
                return change;
            };

            timeField.setTextFormatter(new TextFormatter<>(filter));

            timeField.focusedProperty().addListener((obs, oldF, newF) -> {
                if (!newF) {
                    String txt = timeField.getText().replaceAll("\\D", "");
                    String hh = "00", mm = "00", ss = "00";

                    if (txt.length() <= 2) {
                        hh = String.format("%02d", Integer.parseInt(txt.isEmpty() ? "0" : txt));
                    } else if (txt.length() <= 4) {
                        hh = String.format("%02d", Integer.parseInt(txt.substring(0, 2)));
                        mm = String.format("%02d", Integer.parseInt(txt.substring(2)));
                    } else {
                        hh = String.format("%02d", Integer.parseInt(txt.substring(0, 2)));
                        mm = String.format("%02d", Integer.parseInt(txt.substring(2, 4)));
                        ss = String.format("%02d", Integer.parseInt(txt.substring(4)));
                    }

                    timeField.setText(hh + ":" + mm + ":" + ss);
                }
            });

        } catch (Exception ex) {
            showException("Error initializing Tour Log dialog", ex);
        }
    }

    public void setParentTour(Tour tour) {
        try {
            this.parentTour = tour;
        } catch (Exception ex) {
            showException("Error setting parent tour", ex);
        }
    }

    public void setLog(TourLog log) {
        try {
            vm.setLog(log);
        } catch (Exception ex) {
            showException("Error loading log into dialog", ex);
        }
    }

    public TourLog getLogFromFields() {
        try {
            TourLog log = vm.createLog();
            if (parentTour != null) log.setTour(parentTour);
            log.setDateTime(LocalDateTime.now());
            return log;
        } catch (Exception ex) {
            showException("Invalid log data", ex);
            return null;
        }
    }

    public TourLog getUpdatedLog(TourLog existing) {
        try {
            return vm.updateLog(existing);
        } catch (Exception ex) {
            showException("Error updating log", ex);
            return existing;
        }
    }

    private int countNonDigits(String s, int pos) {
        int count = 0;
        for (int i = 0; i < pos && i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) count++;
        }
        return count;
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