package io.github.moonlightsuite.moonlight.io;

import io.github.moonlightsuite.moonlight.io.parsing.FileType;
import io.github.moonlightsuite.moonlight.io.parsing.ParsingStrategy;

import java.io.*;

/**
 * Generic Class to read line-wise from a text file.
 * After a parsing process executed from a parsing strategy,
 * it returns an object of the given parameter
 *
 * @param <T> type to which the data read is converted
 *
 * @see ParsingStrategy
 */
public class DataReader<T> {
    private final FileType type;
    private final InputStream input;
    private final ParsingStrategy<T> strategy;

    /**
     * Reader initialization
     * @param input the input stream to the data source file
     * @param type the file type of source
     * @param strategy the parsing strategy to execute
     */
    public DataReader(InputStream input, FileType type,
                      ParsingStrategy<T> strategy)
    {
        this.input = input;
        this.type = type;
        this.strategy = strategy;
    }

    /**
     * Reads the data from the source file
     *
     * @return the parsed data after being transformed to T
     */
    public T read() {
        try {
            BufferedReader fileReader =
                            new BufferedReader(new InputStreamReader(input));

            // Read the first line to initialize the parser
            fileReader = readHeader(fileReader);

            // Process the data
            readLines(fileReader);

            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strategy.result();
    }


    /**
     * Header parsing method
     *
     * @param fileReader buffer to the file to read
     * @throws IOException when a new line can't be read
     */
    private BufferedReader readHeader(BufferedReader fileReader)
            throws IOException
    {
        input.mark(0); //TODO: verify that mark is reliable for this
        String header = fileReader.readLine();

        strategy.initialize(splitLine(header));

        // Text files have header that coincides with the first line of data
        // so we reset
        if (FileType.TEXT == type) {
            input.reset();
            fileReader = new BufferedReader(new InputStreamReader(input));
        }

        return fileReader;
    }

    /**
     * Line parsing method
     *
     * @param fileReader buffer to the file to read
     * @throws IOException when the line can't be read
     */
    private void readLines(BufferedReader fileReader) throws IOException {
        String row;
        while (null != (row = fileReader.readLine())) {
            strategy.process(splitLine(row));
        }
    }

    /**
     * It splits the input, according to the standard of the implementation
     * (commas for comma-separated values or spaces for generic text)
     *
     * @param target target input to split, according to implementation
     * @return an array of Strings corresponding to the splitting target
     */
    private String[] splitLine(String target) {
        String[] data;
        if (FileType.CSV == type)
            data = target.split(",");
        else if (FileType.TEXT == type)
            data = target.split(" ");
        else
            throw new UnsupportedFileTypeException(
                    "The file format doesn't comply with the allowed ones.");

        return data;
    }

}


