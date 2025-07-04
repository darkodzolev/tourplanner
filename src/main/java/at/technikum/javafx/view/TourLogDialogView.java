package at.technikum.javafx.view;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.viewmodel.TourLogDialogViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.scene.control.TextFormatter;
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
        // populate options
        difficultyCombo.setItems(vm.getDifficultyOptions());
        ratingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1));
        ratingSpinner.setEditable(true);

        // bind UI ↔ VM
        commentField.textProperty().bindBidirectional(vm.commentProperty());
        difficultyCombo.valueProperty().bindBidirectional(vm.difficultyProperty());
        distanceField.textProperty().bindBidirectional(vm.distanceProperty());
        timeField.textProperty().bindBidirectional(vm.timeProperty());
        ratingSpinner.getValueFactory().valueProperty().bindBidirectional(vm.ratingProperty().asObject());

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String oldText = change.getControlText();
            String newText = change.getControlNewText();

            // Strip non-digits
            String digits = newText.replaceAll("\\D", "");
            if (digits.length() > 6) {
                digits = digits.substring(0, 6);
            }

            // Build formatted string and track where the caret should land
            StringBuilder formatted = new StringBuilder();
            int caretInDigits = change.getCaretPosition()
                    - countNonDigits(newText, change.getCaretPosition());
            caretInDigits = Math.min(caretInDigits, digits.length());

            int newCaretPos = 0;
            for (int i = 0, d = 0; d < digits.length(); ++i) {
                if (i == 2 || i == 5) {
                    formatted.append(':');
                } else {
                    formatted.append(digits.charAt(d++));
                }
                // once we've added the character corresponding to the old caret position,
                // record where the new caret should go
                if (d == caretInDigits) {
                    newCaretPos = formatted.length();
                }
            }

            // Replace the entire text with our formatted result
            change.setRange(0, oldText.length());
            change.setText(formatted.toString());

            // Move both caret and anchor to the computed position
            change.selectRange(newCaretPos, newCaretPos);
            return change;
        };

        timeField.setTextFormatter(new TextFormatter<>(filter));

        timeField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                // on focus lost
                String txt = timeField.getText().replaceAll("\\D", "");
                String hh="00", mm="00", ss="00";
                if (txt.length() <= 2) {
                    hh = String.format("%02d", Integer.parseInt(txt.isEmpty() ? "0" : txt));
                } else if (txt.length() <= 4) {
                    hh = String.format("%02d", Integer.parseInt(txt.substring(0,2)));
                    mm = String.format("%02d", Integer.parseInt(txt.substring(2)));
                } else {
                    hh = String.format("%02d", Integer.parseInt(txt.substring(0,2)));
                    mm = String.format("%02d", Integer.parseInt(txt.substring(2,4)));
                    ss = String.format("%02d", Integer.parseInt(txt.substring(4)));
                }
                timeField.setText(hh + ":" + mm + ":" + ss);
            }
        });
    }

    public void setParentTour(Tour tour) {
        this.parentTour = tour;
    }

    public void setLog(TourLog log) {
        vm.setLog(log);
    }

    public TourLog getLogFromFields() {
        TourLog log = vm.createLog();
        // attach tour
        if (parentTour != null) {
            log.setTour(parentTour);
        }
        // stamp the current date/time so validation passes
        log.setDateTime(LocalDateTime.now());
        return log;
    }

    public TourLog getUpdatedLog(TourLog existing) {
        return vm.updateLog(existing);
    }

    private int countNonDigits(String s, int pos) {
        int count = 0;
        for (int i = 0; i < pos && i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) count++;
        }
        return count;
    }
}