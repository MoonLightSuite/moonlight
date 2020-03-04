package eu.quanticol.moonlight.examples.subway;

import eu.quanticol.moonlight.examples.subway.Parsing.FileType;
import eu.quanticol.moonlight.examples.subway.Parsing.ParsingStrategy;
import eu.quanticol.moonlight.examples.subway.Parsing.UnsupportedFileTypeException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
    private FileType type;
    private String path;
    private ParsingStrategy<T> strategy;

    /**
     * Reader initialization
     * @param path the path to the data source file
     * @param type the file type of source
     * @param strategy the parsing strategy to execute
     */
    public DataReader(String path, FileType type, ParsingStrategy<T> strategy) {
        this.path = path;
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
            BufferedReader fileReader = new BufferedReader(new FileReader(path));

            // Read the first line to initialize the parser
            readHeader(fileReader);

            // Process the data
            readLines(fileReader);

            fileReader.close();
        } catch (IOException | UnsupportedFileTypeException e) {
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
    private void readHeader(BufferedReader fileReader) throws IOException {
        // Warning: hack!! Headers longer than 500 chars will break this method!
        int headerLimit = 500;
        fileReader.mark(headerLimit);
        String header = fileReader.readLine();
        if (headerLimit < header.length())
            throw new IOException("File's first line exceeds the maximum string length of " + headerLimit);

        strategy.initialize(splitLine(header));

        if (FileType.TEXT == type)
            fileReader.reset();
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

    private String[] splitLine(String line) {
        String[] data;
        if (FileType.CSV == type)
            data = line.split(",");
        else if (FileType.TEXT == type)
            data = line.split(" ");
        else
            throw new UnsupportedFileTypeException("The file format doesn't comply with the allowed ones.");

        return data;
    }

}


