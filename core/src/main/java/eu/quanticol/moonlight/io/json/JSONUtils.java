package eu.quanticol.moonlight.io.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;

class JSONUtils {

    private JSONUtils() {
        //Utility class
    }

    static Object[][] getValues(Double[] times, String[] variables, Class<?>[] varTypes, JsonArray signals, JsonDeserializationContext arg2) {
        Object[][] toReturn = new Object[times.length][variables.length];
        JsonArray[] values = new JsonArray[variables.length];
        for (int i=0 ; i<variables.length ; i++ ) {
            values[i] = signals.get(i).getAsJsonObject().get("values").getAsJsonArray();
        }
        for (int i=0 ; i<times.length; i++) {
            for (int j=0; j<variables.length; j++ ) {
                toReturn[i][j] = arg2.deserialize(values[j].get(i), varTypes[j]);
            }
        }
        return toReturn;
    }

    static String[] getVariables(JsonArray jsa) {
        String[] variables = new String[jsa.size()];
        for( int i=0 ; i<variables.length ; i++) {
            variables[i] = jsa.get(i).getAsJsonObject().get("name").getAsString();
        }
        return variables;
    }

    static <T> T[] toArray(JsonArray jArray, Class<T> varType, T[] target, JsonDeserializationContext arg2) {
        for( int i=0 ; i<target.length ; i++ ) {
            target[i] = arg2.deserialize(jArray.get(i), varType);
        }
        return target;
    }

    static Class<?> getVariableType(String str) {
        if ("boolean".equals(str)) {
            return Boolean.class;
        }
        if ("real".equals(str)) {
            return Double.class;
        }
        if ("integer".equals(str)) {
            return Integer.class;
        }
        throw new IllegalArgumentException("Unknown signal type "+str);
    }
}