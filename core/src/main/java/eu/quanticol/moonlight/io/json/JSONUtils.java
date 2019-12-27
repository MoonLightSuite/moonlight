package eu.quanticol.moonlight.io.json;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;

import eu.quanticol.moonlight.signal.AssignmentFactory;

class JSONUtils {

    private JSONUtils() {
        //Utility class
    }
    
    static boolean checkVariables( AssignmentFactory factory , JsonArray signals ) {
    	int size = signals.size();
    	factory.checkNumberOfVariables( size );
    	for( int i=0 ; i<size ; i++ ) {
    		factory.checkVariableType( signals.get(i).getAsJsonObject().get("name").getAsString(), 
    				signals.get(i).getAsJsonObject().get("type").getAsString()
    				);
    	}
    	return true;
    }
    
    static Map<String,String> getAssignmentMap( String[] variables, int i, JsonArray signals ) {
    	HashMap<String,String> toReturn = new HashMap<>();
    	for( int j=0 ; j<variables.length; j++ ) {    		
    		toReturn.put(variables[j], signals.get(j).getAsJsonObject().get("values").getAsJsonArray().get(i).getAsString());
    	}
    	return toReturn;
    }

    
//    static Object[][] getValues(Double[] times, String[] variables, Class<?>[] varTypes, JsonArray signals, JsonDeserializationContext arg2) {
//        Object[][] toReturn = new Object[times.length][variables.length];
//        JsonArray[] values = new JsonArray[variables.length];
//        for (int i=0 ; i<variables.length ; i++ ) {
//            values[i] = signals.get(i).getAsJsonObject().get("values").getAsJsonArray();
//        }
//        for (int i=0 ; i<times.length; i++) {
//            for (int j=0; j<variables.length; j++ ) {
//                toReturn[i][j] = arg2.deserialize(values[j].get(i), varTypes[j]);
//            }
//        }
//        return toReturn;
//    }

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
        if ("string".equals(str)) {
            return String.class;
        }
        throw new IllegalArgumentException("Unknown signal type "+str);
    }

    private Class<?>[] getSignalsType(JsonArray jsa) {
        Class<?>[] types = new Class<?>[jsa.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = JSONUtils.getVariableType(jsa.get(i).getAsJsonObject().get("type").getAsString());
        }
        return types;
    }

}