package eu.quanticol.moonlight.io.json;

import com.google.gson.GsonBuilder;

class JSONReader<T> {

    private final GsonBuilder gson;
    private Class<T> generatedClass;

    JSONReader(Object deserializerClass, Class<T> generatedClass) {
        this.generatedClass = generatedClass;
        this.gson = new GsonBuilder();
        this.gson.registerTypeAdapter(generatedClass, deserializerClass);
    }

    DeserializerFunction<T> getDeserializer() {
        return json -> gson.create().fromJson(json, generatedClass);
    }
}