package eu.quanticol.moonlight.examples.subway.data;

import eu.quanticol.moonlight.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class HashBiMap<K1, K2, V> extends HashMap<Pair<K1, K2>, V> {

    public V put(K1 key1, K2 key2, V value) {
        Pair<K1, K2> key = new Pair<>(key1, key2);
        return put(key, value);
    }

    public V get(K1 key1, K2 key2) {
        Pair<K1, K2> key = new Pair<>(key1, key2);
        return get(key);
    }

    public Set<K1> keySet1() {
        Set<K1> key1Set =  new HashSet<>();

        for(Pair<K1, K2> key : super.keySet()) {
            key1Set.add(key.getFirst());
        }

        return key1Set;
    }

    public Set<K2> keySet2() {
        Set<K2> key2Set =  new HashSet<>();

        for(Pair<K1, K2> key : super.keySet()) {
            key2Set.add(key.getSecond());
        }

        return key2Set;
    }


}
