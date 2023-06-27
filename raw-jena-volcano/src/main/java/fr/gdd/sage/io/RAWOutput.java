package fr.gdd.sage.io;

import org.apache.jena.dboe.trans.bplustree.RAWJenaIterator;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.iterator.RAWJenaIteratorWrapper;

import java.util.*;

/**
 * Objects being return to the client.
 */
public class RAWOutput {

    Integer nbScans = 0;

    List<HashMap<Integer, Long>> cardinalities = new ArrayList<>();
    List<Set<Node>> bindings = new ArrayList<>();

    public RAWOutput() {}

    /**
     * Another scan has been performed on a {@link org.apache.jena.dboe.trans.bplustree.BPlusTree}.
     */
    public void addScan() {
        nbScans += 1;
    }

    public Integer getNbScans() {
        return nbScans;
    }

    /**
     * The root registers a new random result.
     * @param iterators The map id_of_iterator to actual iterator.
     */
    public void addResultThenClear(HashMap<Integer, RAWJenaIteratorWrapper> iterators) {
        HashSet<Node> b = new HashSet<>();
        HashMap<Integer, Long> c = new HashMap<>();
        for (Map.Entry<Integer, RAWJenaIteratorWrapper> kv : iterators.entrySet()) {
            b.addAll(kv.getValue().getCurrent().asList());
            c.put(kv.getKey(), ((RAWJenaIterator) kv.getValue().getWrapped()).cardinality());

        }
        iterators.clear();
        cardinalities.add(c);
        bindings.add(b);
    }

}
