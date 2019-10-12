package eu.quanticol.moonlight.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ObjectSerializerTest {

    @Test
    void testSerializeAndDeserializer(@TempDir Path tempDir) throws IOException, ClassNotFoundException {
        double[] object = new double[2];
        Path resolve = tempDir.resolve("vector.storage");

        ObjectSerializer.serialize(object, resolve.toString());
        double[] deserialize = ObjectSerializer.deserialize(resolve.toString(), double[].class);

        assertArrayEquals(object, deserialize);
    }

}