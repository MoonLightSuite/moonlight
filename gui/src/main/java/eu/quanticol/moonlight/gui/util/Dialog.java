package eu.quanticol.moonlight.gui.util;

/**
 * Interface for a dialog window
 */
public interface Dialog {

    /**
     * Dialog for a warning message
     * @param message message
     */
    void warning(String message);

    /**
     * Dialog for an info message
     * @param message message
     */
    void info(String message);

    /**
     * Dialog for an error message
     * @param message message
     */
    void error(String message);

}
