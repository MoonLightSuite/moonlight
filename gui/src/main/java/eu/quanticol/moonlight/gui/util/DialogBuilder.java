package eu.quanticol.moonlight.gui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;

/**
 * Class that implements {@link Dialog} interface to show dialog window
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class DialogBuilder implements Dialog {

    private final String theme;

    public DialogBuilder(String theme) {
        this.theme = theme;
    }

    /**
     * Dialog for a warning message
     *
     * @param message message
     */
    @Override
    public void warning(String message) {
        Alert warn = new Alert(Alert.AlertType.WARNING);
        generateDialog(message, warn);
    }

    /**
     * Dialog for an info message
     *
     * @param message message
     */
    @Override
    public void info(String message) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        generateDialog(message, info);
    }

    /**
     * Dialog for an error message
     *
     * @param message message
     */
    @Override
    public void error(String message) {
        Alert err = new Alert(Alert.AlertType.ERROR);
        generateDialog(message, err);
    }

    /**
     * Generate a dialog window that closes after some seconds
     *
     * @param message message to show
     * @param dialog  type of dialog
     */
    private void generateDialog(String message, Alert dialog) {
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        dialog.initStyle(StageStyle.UNDECORATED);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(theme);
        dialogPane.getStyleClass().add("dialog");
        dialog.showAndWait();
    }
}
