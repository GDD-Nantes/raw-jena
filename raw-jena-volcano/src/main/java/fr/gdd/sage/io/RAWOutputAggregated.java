package fr.gdd.sage.io;

import fr.gdd.sage.arq.IdentifierAllocator;
import org.apache.jena.sparql.engine.iterator.RAWJenaIteratorWrapper;
import org.apache.jena.tdb.store.Hash;

import java.util.HashMap;
import java.util.Map;

/**
 * An aggregated version of the RAndom Walks output. It associates with every scan identifier its cardinality
 * and the number of walks.
 */
public class RAWOutputAggregated {

    HashMap<Integer, Long> node2cardinality = new HashMap<>();
    HashMap<Integer, Long> node2nbWalks = new HashMap<>();

    public RAWOutputAggregated() { }

    public void addResult(HashMap<Integer, RAWJenaIteratorWrapper> iterators) {
        for (Map.Entry<Integer, RAWJenaIteratorWrapper> kv: iterators.entrySet()) {
            if (!node2cardinality.containsKey(kv.getKey())) {
                node2cardinality.put(kv.getKey(), 0L);
            }
            node2cardinality.put(kv.getKey(), node2cardinality.get(kv.getKey()) + kv.getValue().getCardinality());

            if (!node2nbWalks.containsKey(kv.getKey())) {
                node2nbWalks.put(kv.getKey(), 0L);
            }
            node2nbWalks.put(kv.getKey(), node2nbWalks.get(kv.getKey()) + 1);
        }
    }

}
