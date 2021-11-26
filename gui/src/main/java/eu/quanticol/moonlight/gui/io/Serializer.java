package eu.quanticol.moonlight.gui.io;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Class of serializing and deserializing for Json.
 *
 * @author Albanese Clarissa, Sorritelli Greta
 */
public final class Serializer<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    private final Class<T> implementationClass;

    private Serializer(final Class<T> implementationClass) {
        this.implementationClass = implementationClass;
    }

    public static <T> Serializer<T> interfaceSerializer(final Class<T> implementationClass) {
        return new Serializer<>(implementationClass);
    }

    @Override
    public T deserialize(final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context) {
        return context.deserialize(jsonElement, implementationClass);
    }

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        final Type targetType = src != null
                ? src.getClass()
                : typeOfSrc;
        return context.serialize(src, targetType);
    }
}
