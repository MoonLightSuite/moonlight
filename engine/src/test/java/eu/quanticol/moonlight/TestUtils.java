package eu.quanticol.moonlight;

import eu.quanticol.moonlight.core.signal.Sample;
import eu.quanticol.moonlight.online.signal.TimeChain;
import eu.quanticol.moonlight.online.signal.TimeSegment;
import eu.quanticol.moonlight.online.signal.Update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestUtils {
    /**
     * Note: this will be removed when moving to Java 9+
     */
    @SafeVarargs
    public static <E> List<E> listOf(E...elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    public static <T extends Comparable<T> & Serializable, V>
    List<TimeChain<T, V>> toChains(List<Update<T, V>> ups)
    {
        List<TimeChain<T, V>> result = new ArrayList<>();

        for(int i = 0; i < ups.size(); i++) {
            Sample<T, V> s = new TimeSegment<>(ups.get(i).getStart(),
                    ups.get(i).getValue());
            if(i > 0 && s.getStart().equals(ups.get(i - 1).getEnd())) {
                int lastIndex = result.size() - 1;
                TimeChain<T, V> last = result.get(lastIndex);
                TimeChain<T, V> newChain = new TimeChain<>(last.toList(),
                        ups.get(i).getEnd());
                newChain.add(s);
                result.set(lastIndex, newChain);
            } else {
                result.add(new TimeChain<>(s, ups.get(i).getEnd()));
            }
        }

        return result;
    }
}
