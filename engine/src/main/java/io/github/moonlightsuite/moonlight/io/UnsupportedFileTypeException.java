package io.github.moonlightsuite.moonlight.io;

import io.github.moonlightsuite.moonlight.io.parsing.FileType;
import io.github.moonlightsuite.moonlight.io.parsing.ParsingStrategy;

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
