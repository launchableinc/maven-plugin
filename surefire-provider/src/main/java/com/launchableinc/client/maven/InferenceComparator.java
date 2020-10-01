package com.launchableinc.client.maven;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InferenceComparator implements Comparator<Class<?>> {

    final Map<String, Integer> orderedMap = new HashMap<>();

    InferenceComparator(List<String> orderedList) {
        int index = 0;
        for (String testClassName : orderedList ) {
            this.orderedMap.put(testClassName, Integer.valueOf(index++));
        }
    }

    @Override
    public int compare(Class<?> c1, Class<?> c2) {
        // The unknown test is a high priority.
        // orderedList.indexOf(unknown test) -> -1
        int c1Index = this.orderedMap.containsKey(c1.getName()) ? this.orderedMap.get(c1.getName()).intValue() : -1;
        int c2Index = this.orderedMap.containsKey(c2.getName()) ? this.orderedMap.get(c2.getName()).intValue() : -1;

        return c1Index - c2Index;
    }
}
