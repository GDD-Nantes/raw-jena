package fr.gdd.sage.io;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonRootName;
import fr.gdd.sage.arq.IdentifierAllocator;
import org.apache.jena.sparql.engine.iterator.RAWJenaIteratorWrapper;
import org.apache.jena.tdb.store.Hash;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * An aggregated version of the RAndom Walks output. It associates with every scan identifier its cardinality
 * and the number of walks.
 */
public class RAWOutputAggregated implements Serializable {

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

    public HashMap<Integer, Long> getNode2cardinality() {
        return node2cardinality;
    }

    public HashMap<Integer, Long> getNode2nbWalks() {
        return node2nbWalks;
    }
}
