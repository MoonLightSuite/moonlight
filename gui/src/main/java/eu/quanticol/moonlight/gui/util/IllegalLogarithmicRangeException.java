package eu.quanticol.moonlight.gui.util;

/**
 * Exception to be thrown when a bound value isn't supported by the logarithmic axis
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public class IllegalLogarithmicRangeException extends RuntimeException {

    public IllegalLogarithmicRangeException(String message) {
            super(message);
        }
}
