package eu.quanticol.moonlight.script;

import java.util.HashMap;
import java.util.Map;

public class DefaultNameResolver implements NameResolver {

    private final MoonLightEnumerationRepository repository;
    private final Map<String, Double> constants;

    public DefaultNameResolver(MoonLightEnumerationRepository repository, Map<String, Double> constants) {
        this.repository = repository;
        this.constants = constants;
    }


    @Override
    public double get(String name) {
        double value = repository.valueOf(name);
        if (value >=0 ) {
            return value;
        }
        if (constants.containsKey(name)) {
            return constants.get(name);
        }
        return Double.NaN;
    }
}
