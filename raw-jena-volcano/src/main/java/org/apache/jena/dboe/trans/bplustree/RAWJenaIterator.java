package org.apache.jena.dboe.trans.bplustree;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.dboe.base.buffer.RecordBuffer;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.tdb2.lib.TupleLib;
import org.apache.jena.tdb2.store.NodeId;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * This {@link RAWJenaIterator} enables random exploration of patterns.
 * This heavily depends on the {@link BPlusTree}
 * data structure, since it relies on {@link AccessPath} to
 * find out the boundary of the scan and draw a random element from
 * it.
 * <br />
 * This is inspired from {@link BPTreeRangeIterator}.
 **/
public class RAWJenaIterator implements Iterator<Tuple<NodeId>> {
    private final ProgressJenaIterator wrapped;

    private Tuple<NodeId> current;
    private final TupleMap tupleMap;

    private boolean first = true;

    public RAWJenaIterator(PreemptTupleIndexRecord ptir, Record minRec, Record maxRec) {
        this.wrapped = new ProgressJenaIterator(ptir, minRec, maxRec);
        this.tupleMap = ptir.tupleMap;
    }

    public boolean hasNext() {
        if (first && random()) {
            first = false;
            return true;
        } else {
            return false;
        }
    }

    public Tuple<NodeId> next() {
        return current;
    }

    /**
     * `random()` modifies the behavior of the iterator so that each
     * `next()` provides a new random binding within the interval.
     * Beware that it does not terminate nor ensure distinct bindings.
     * <br />
     * As for `cardinality()`, it uses the underlying balanced tree to
     * efficiently reach a Record between two access paths.
     *
     * @return True if the random can return a solution, false otherwise.
     */
    public boolean random() {
        // computing random steps from the root to a leaf
        Record currentRecord = wrapped.getRandom();
        if (Objects.isNull(currentRecord)) {
            current = null;
            return false;
        } else {
            current = TupleLib.tuple(currentRecord, tupleMap);
            return true;
        }
    }

    public double cardinality(Integer... sample) {
        return wrapped.cardinality(sample);
    }

}
