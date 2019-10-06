package eu.quanticol.moonlight.io.json;

@FunctionalInterface
public interface DeserializerFunction<T> {

    T deserialize(String json) ;
}
