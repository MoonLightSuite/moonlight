package eu.quanticol.moonlight.script;

import eu.quanticol.moonlight.offline.signal.EnumerationHandler;

import java.util.HashMap;
import java.util.Map;

public class MoonLightEnumerationRepository {

    private final Map<String, EnumerationHandler<String>> archive;
    private final Map<String, Integer> indexesOfelements;

    public MoonLightEnumerationRepository(Map<String, EnumerationHandler<String>> archive, Map<String, Integer> indexesOfelements) {
        this.archive = archive;
        this.indexesOfelements = indexesOfelements;
    }

    public MoonLightEnumerationRepository() {
        this(new HashMap<>(), new HashMap<>());
    }

    public void add(String name, String[] elements) {
        EnumerationHandler<String> handler = new EnumerationHandler<>(String.class, elements);
        archive.put(name, handler);
        for (String e: elements) {
            indexesOfelements.put(e, handler.indexOf(e));
        }
    }

    public EnumerationHandler<String> getHandler(String name) {
        return archive.get(name);
    }

    public int valueOf(String element) {
        return indexesOfelements.getOrDefault(element, -1);
    }
}
