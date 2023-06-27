package fr.gdd.sage.io;

import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.RAWJenaIteratorWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Objects being returned to the client. It contains the most of random walks: individual bindings
 * with their respective cardinality. Of course, it is also the most expensive, especially on
 * traffic since it may be sent through the network.
 * <br />
 * Depending on the application, it may be wiser to get an aggregated version of these output.
 */
public class RAWOutput {

    Integer nbScans = 0;

    List<HashMap<Integer, Long>> cardinalities = new ArrayList<>();
    List<Binding> bindings = new ArrayList<>();

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
        BindingBuilder b = BindingBuilder.create();
        HashMap<Integer, Long> c = new HashMap<>();
        for (Map.Entry<Integer, RAWJenaIteratorWrapper> kv : iterators.entrySet()) {
            b.addAll(kv.getValue().getCurrent());
            c.put(kv.getKey(), kv.getValue().getCardinality());
        }
        iterators.clear();
        cardinalities.add(c);
        bindings.add(b.build());
    }

}
