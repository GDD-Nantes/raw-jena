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

    // wrapped instead of `extends` since they have same function
    // name `next()` with different return type. Otherwise, could
    // create constructors for NullIterator and SingletonIterator.
    // (TODO) think about that.
    private final ProgressJenaIterator wrapped;

    private final Record minRecord;
    private final Record maxRecord;

    private Tuple<NodeId> current;
    private final TupleMap tupleMap;

    private boolean first = true;

    public RAWJenaIterator(PreemptTupleIndexRecord ptir, Record minRec, Record maxRec) {
        this.wrapped = new ProgressJenaIterator(ptir, minRec, maxRec);
        BPTreeNode root = ptir.bpt.getNodeManager().getRead(ptir.bpt.getRootId());
        this.tupleMap = ptir.tupleMap;
        this.minRecord = Objects.isNull(minRec) ? root.minRecord() : minRec;
        this.maxRecord = Objects.isNull(maxRec) ? root.maxRecord() : maxRec;
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
        AccessPath randomPath = wrapped.randomWalk();

        // picking a random record in the leaf's page
        List<AccessPath.AccessStep> randomSteps = randomPath.getPath();

        AccessPath.AccessStep lastStep = randomSteps.get(randomSteps.size() - 1);
        RecordBuffer recordBuffer = ((BPTreeRecords) lastStep.page).getRecordBuffer();

        int idxMin = recordBuffer.find(minRecord);
        int idxMax = recordBuffer.find(maxRecord);
        
        idxMin = idxMin < 0 ? -idxMin - 1 : idxMin;
        idxMax = idxMax < 0 ? -idxMax - 1 : idxMax;

        if (idxMin == idxMax) {
            return false; // No result for this random step
        }

        int randomInRecords = (int) (idxMin + Math.random() * (idxMax - idxMin)); // no need for -1 in a `RecordBuffer`
        Record currentRecord = recordBuffer.get(randomInRecords);

        current = TupleLib.tuple(currentRecord, tupleMap);

        return true;
    }

    public long cardinality(Integer... sample) {
        return wrapped.cardinality(sample);
    }

}
