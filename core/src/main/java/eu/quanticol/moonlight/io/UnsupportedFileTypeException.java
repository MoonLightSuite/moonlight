package eu.quanticol.moonlight.io;

import eu.quanticol.moonlight.io.parsing.FileType;
import eu.quanticol.moonlight.io.parsing.ParsingStrategy;

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
