package eu.quanticol.moonlight.util;

import java.io.*;

public class ObjectSerializer {

    private ObjectSerializer() {
        //utility class
    }

    public static void serialize(Object object, String path) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(path)))) {
            oos.writeObject(object);
        }
    }

    public static <T> T deserialize(String objectPath, Class<T> objectType) throws IOException, ClassNotFoundException {
        try (ObjectInputStream iis = new ObjectInputStream(new FileInputStream(objectPath))) {
            return (T) iis.readObject();
        }
    }

}