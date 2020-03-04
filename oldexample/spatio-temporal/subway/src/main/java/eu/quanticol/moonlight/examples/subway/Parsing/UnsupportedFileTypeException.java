package eu.quanticol.moonlight.examples.subway.Parsing;

/**
 * Exception raised when trying to parse an unsupported file type.
 *
 * @see ParsingStrategy
 * @see FileType
 */
public class UnsupportedFileTypeException extends RuntimeException {

    public UnsupportedFileTypeException(String message) {
        super(message);
    }
}
