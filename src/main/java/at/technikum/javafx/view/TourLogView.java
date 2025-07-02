package at.technikum.javafx.view;

import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.viewmodel.TourLogViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import java.util.function.UnaryOperator;
import javafx.scene.control.TextFormatter;

public class TourLogView implements Initializable {

    private final TourLogViewModel viewModel;

    public TourLogView(TourLogViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @FXML private ListView<TourLog> logList;
    @FXML private Button newLogButton, editLogButton, deleteLogButton;
    @FXML private TextArea commentField;
    @FXML private ComboBox<String> difficultyBox;
    @FXML private TextField logDistanceField, logTimeField;
    @FXML private Spinner<Integer> ratingSpinner;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // bind list of logs
        logList.setItems(viewModel.getLogs());

        // sync selected log
        viewModel.selectedLogProperty().bind(logList.getSelectionModel().selectedItemProperty());

        // populate fields when a log is selected
        viewModel.selectedLogProperty().addListener((obs, oldLog, newLog) -> {
            if (newLog != null) {
                commentField.setText(newLog.getComment());
                difficultyBox.setValue(newLog.getDifficulty());
                logDistanceField.setText(String.valueOf(newLog.getTotalDistance()));
                logTimeField.setText(newLog.getTotalTime());
                ratingSpinner.getValueFactory().setValue(newLog.getRating());
            } else {
                clearFields();
            }
        });

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String oldText = change.getControlText();
            String newText = change.getControlNewText();

            // Strip non-digits
            String digits = newText.replaceAll("\\D", "");
            if (digits.length() > 6) {
                digits = digits.substring(0, 6);
            }

            // Build formatted string and track where we should put the caret
            StringBuilder formatted = new StringBuilder();
            int caretInDigits = change.getCaretPosition()
                    - countNonDigits(newText, change.getCaretPosition());
            // clamp
            caretInDigits = Math.min(caretInDigits, digits.length());

            int newCaretPos = 0;
            for (int i = 0, d = 0; d < digits.length(); ++i) {
                if (i == 2 || i == 5) {
                    formatted.append(':');
                } else {
                    formatted.append(digits.charAt(d++));
                }
                // advance caret mapping: if we passed the original digit caret index,
                // mark this as the new caret position
                if (d == caretInDigits) {
                    newCaretPos = formatted.length();
                }
            }

            // Prepare the change to replace the whole text
            change.setRange(0, oldText.length());
            change.setText(formatted.toString());

            // Place both anchor and caret at newCaretPos
            change.selectRange(newCaretPos, newCaretPos);
            return change;
        };

        TextFormatter<String> formatter = new TextFormatter<>(filter);
        logTimeField.setTextFormatter(formatter);

        // setup rating spinner
        ratingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1));
        ratingSpinner.setEditable(true);

        // button handlers
        newLogButton.setOnAction(e -> handleNewLog());
        editLogButton.setOnAction(e -> handleEditLog());
        deleteLogButton.setOnAction(e -> handleDeleteLog());
    }

    private void handleNewLog() {
        try {
            LocalDateTime now = LocalDateTime.now();
            String comment = commentField.getText();
            if (comment.isBlank()) {
                showAlert("Validation error", "Comment is required.");
                return;
            }
            String difficulty = difficultyBox.getValue();
            if (difficulty == null) {
                showAlert("Validation error", "Difficulty is required.");
                return;
            }
            double dist = Double.parseDouble(logDistanceField.getText());
            String totTime = logTimeField.getText();
            int rating = ratingSpinner.getValue();

            TourLog log = new TourLog();
            log.setTour(viewModel.getSelectedTour());
            log.setDateTime(now);
            log.setComment(comment);
            log.setDifficulty(difficulty);
            log.setTotalDistance(dist);
            log.setTotalTime(totTime);
            log.setRating(rating);

            viewModel.createLog(log);
        } catch (NumberFormatException ex) {
            showAlert("Invalid input", "Distance must be a valid number.");
        } catch (IllegalArgumentException ex) {
            showAlert("Validation error", ex.getMessage());
        }
    }

    private void handleEditLog() {
        TourLog log = viewModel.selectedLogProperty().get();
        if (log == null) {
            showAlert("Selection error", "No log selected to edit.");
            return;
        }
        try {
            // preserve original timestamp
            String comment = commentField.getText();
            if (comment.isBlank()) {
                showAlert("Validation error", "Comment is required.");
                return;
            }
            String difficulty = difficultyBox.getValue();
            if (difficulty == null) {
                showAlert("Validation error", "Difficulty is required.");
                return;
            }
            double dist = Double.parseDouble(logDistanceField.getText());
            String totTime = logTimeField.getText();
            int rating = ratingSpinner.getValue();

            log.setComment(comment);
            log.setDifficulty(difficulty);
            log.setTotalDistance(dist);
            log.setTotalTime(totTime);
            log.setRating(rating);

            viewModel.updateLog(log);
        } catch (NumberFormatException ex) {
            showAlert("Invalid input", "Distance must be a valid number.");
        } catch (IllegalArgumentException ex) {
            showAlert("Validation error", ex.getMessage());
        }
    }

    private void handleDeleteLog() {
        TourLog log = viewModel.selectedLogProperty().get();
        if (log == null) {
            showAlert("Selection error", "No log selected to delete.");
            return;
        }
        viewModel.deleteLog(log);
    }

    private void clearFields() {
        commentField.clear();
        difficultyBox.setValue(null);
        logDistanceField.clear();
        logTimeField.clear();
        ratingSpinner.getValueFactory().setValue(1);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private int countNonDigits(String s, int pos) {
        int count = 0;
        for (int i = 0; i < pos && i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) count++;
        }
        return count;
    }
}