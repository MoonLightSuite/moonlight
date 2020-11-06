package eu.quanticol.moonlight.examples.subway.io;

import eu.quanticol.moonlight.examples.subway.parsing.ParsingStrategy;

import java.io.*;

/**
 * Generic Class to write line-wise from a text file.
 * given an object conformant to a parsing strategy,
 * it updates the output file
 *
 * @param <T> type to which the data read is converted
 *
 * @see ParsingStrategy
 */
public class DataWriter<T> {

    private final FileType type;
    private final String path;
    private final ParsingStrategy<T> strategy;

    /**
     * Writer initialization
     * @param path the path to the data source file
     * @param type the file type of source
     * @param strategy the parsing strategy to execute
     */
    public DataWriter(String path, FileType type, ParsingStrategy<T> strategy) {
        this.path = path;
        this.type = type;
        this.strategy = strategy;
    }

    /**
     * Writes the data to the destination file
     * @param data to write
     */
    public void write(T data) {
        try {
            BufferedWriter outWriter = new BufferedWriter(new FileWriter(path));

            // Write the data
            writeData(outWriter, data);

            outWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeData(BufferedWriter outWriter, T data) {
        outWriter.write("");
    }
}
