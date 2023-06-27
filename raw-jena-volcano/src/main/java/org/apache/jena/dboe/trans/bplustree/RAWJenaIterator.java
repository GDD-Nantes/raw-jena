package org.apache.jena.dboe.trans.bplustree;

import fr.gdd.sage.interfaces.RandomIterator;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.dboe.base.buffer.RecordBuffer;
import org.apache.jena.dboe.base.record.Record;
import org.apache.jena.graph.Node;
import org.apache.jena.tdb2.lib.TupleLib;
import org.apache.jena.tdb2.store.NodeId;

import java.util.*;

/**
 * This {@link RAWJenaIterator} enables random exploration of patterns.
 * This heavily depends on the {@link BPlusTree}
 * data structure, since it relies on {@link AccessPath} to
 * find out the boundary of the scan and draw a random element from
 * it.
 *
 * This is inspired from {@link BPTreeRangeIterator}.
 **/
public class RAWJenaIterator implements Iterator<Tuple<NodeId>>, RandomIterator {

    private static final int NB_WALKS = 2;

    private final Record minRecord;
    private final Record maxRecord;

    private Tuple<NodeId> current;
    private TupleMap tupleMap;
    private BPTreeNode root;

    private boolean first = true;

    public RAWJenaIterator(PreemptTupleIndexRecord ptir, Record minRec, Record maxRec) {
        this.root = ptir.bpt.getNodeManager().getRead(ptir.bpt.getRootId());
        this.tupleMap = ptir.tupleMap;
        this.minRecord = Objects.isNull(minRec) ? root.minRecord() : minRec;
        this.maxRecord = Objects.isNull(maxRec) ? root.maxRecord() : maxRec;
    }

    @Override
    public boolean hasNext() {
        if (first && random()) {
            first = false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Tuple<NodeId> next() {
        return current;
    }

    public Tuple<NodeId> getCurrent() {
        return current;
    }

    /**
     * Performs random steps from the root to a leaf of a B+tree index.
     * 
     * @return An `AccessPath` built using random steps.
     */
    private AccessPath randomWalk() {
        AccessPath minPath = new AccessPath(null);
        AccessPath maxPath = new AccessPath(null);

        root.internalSearch(minPath, minRecord);
        root.internalSearch(maxPath, maxRecord);

        assert minPath.getPath().size() == maxPath.getPath().size();

        // System.out.println("minSteps: " + minPath.getPath());
        // System.out.println("maxSteps: " + maxPath.getPath());

        AccessPath randomPath = new AccessPath(null);

        AccessPath.AccessStep minStep = minPath.getPath().get(0);
        AccessPath.AccessStep maxStep = maxPath.getPath().get(0);
        
        int idxRnd = (int) (minStep.idx + Math.random() * (maxStep.idx - minStep.idx + 1));
        randomPath.add(minStep.node, idxRnd, minStep.node.get(idxRnd));
        AccessPath.AccessStep lastStep = randomPath.getPath().get(randomPath.getPath().size() - 1);
        
        while (!lastStep.node.isLeaf()) {
            BPTreeNode node = (BPTreeNode) lastStep.page;

            int idxMin = node.findSlot(minRecord);
            int idxMax = node.findSlot(maxRecord);

            idxMin = idxMin < 0 ? -idxMin - 1 : idxMin;
            idxMax = idxMax < 0 ? -idxMax - 1 : idxMax;

            idxRnd = (int) (idxMin + Math.random() * (idxMax - idxMin + 1));
            randomPath.add(node, idxRnd, node.get(idxRnd));
            lastStep = randomPath.getPath().get(randomPath.getPath().size() - 1);
        }

        // System.out.println("randomPath: " + randomPath);

        assert randomPath.getPath().size() == minPath.getPath().size();

        return randomPath;
    } 

    /**
     * Estimates the cardinality of a triple/quad pattern knowing that
     * the underlying data structure is a balanced tree.
     * When the number of results is small, more precision is needed.
     * Fortunately, this often means that results are spread among one
     * or two pages, which allows us to precisely count using binary search.
     *
     * (TODO) Take into account possible deletions.
     * (TODO) Triple patterns that return no solutions need to be handle elsewhere. Is it the case?
     * 
     * @return An estimated cardinality.
     */
    @Override
    public long cardinality() {
        // System.out.println("minRecord: " + minRecord);
        // System.out.println("maxRecord: " + maxRecord);

        AccessPath minPath = new AccessPath(null);
        AccessPath maxPath = new AccessPath(null);

        root.internalSearch(minPath, minRecord);
        root.internalSearch(maxPath, maxRecord);

        List<AccessPath.AccessStep> minSteps = minPath.getPath();
        List<AccessPath.AccessStep> maxSteps = maxPath.getPath();

        assert minSteps.size() == maxSteps.size();

        long[] pageSize = new long[minSteps.size() + 1];
        
        // estimating the size of B+tree's pages and nodes using the min and max access path
        // for (int i = 0; i < minSteps.size(); i++) {
        //     AccessPath.AccessStep minStep = minSteps.get(i);
        //     AccessPath.AccessStep maxStep = maxSteps.get(i);
        //     pageSize[i] += minStep.node.getCount();
        //     pageSize[i] += maxStep.node.getCount();
        //     pageSize[i] /= 2;
        // }
        // pageSize[pageSize.length - 1] += minSteps.get(minSteps.size() - 1).page.getCount();
        // pageSize[pageSize.length - 1] += maxSteps.get(maxSteps.size() - 1).page.getCount();
        // pageSize[pageSize.length - 1] /= 2;

        // estimating the size of B+tree's pages and nodes using random walks
        for (int i = 0; i < NB_WALKS; i++) {
            AccessPath path = randomWalk();
            int j = 0;
            for (; j < path.getPath().size(); j++) {
                pageSize[j] += path.getPath().get(j).node.getCount();
            }
            pageSize[j] += path.getPath().get(j - 1).page.getCount();
        }
        for (int i = 0; i < pageSize.length; i++) {
            pageSize[i] /= NB_WALKS;
        }

        long cardinality = 0;

        for (int i = 0; i < minSteps.size(); i++) {
            AccessPath.AccessStep minStep = minSteps.get(i);
            AccessPath.AccessStep maxStep = maxSteps.get(i);

            // System.out.println("minStep: " + minStep);
            // System.out.println("maxStep: " + maxStep);

            // System.out.println("idxMin: " + minStep.idx);
            // System.out.println("idxMax: " + maxStep.idx);

            // System.out.println("minNode: " + minStep.node.id);
            // System.out.println("maxNode: " + maxStep.node.id);

            // System.out.println("minPage: " + minStep.page.getId());
            // System.out.println("maxPage: " + maxStep.page.getId());

            if (!equalsStep(minStep, maxStep)) {
                long branchingFactor = 1;
                for (int j = i + 1; j < pageSize.length; j++) {
                    branchingFactor *= pageSize[j];
                }     
                if (!minStep.node.isLeaf()) {
                    branchingFactor += pageSize[pageSize.length - 1]; // (TODO): Why is it necessary?
                }
                
                if (minStep.node.id == maxStep.node.id) {
                    cardinality += (maxStep.idx - minStep.idx - 1) * branchingFactor;
                } else {
                    cardinality += (minStep.node.getCount() - minStep.idx) * branchingFactor;
                    cardinality += maxStep.idx * branchingFactor;
                }
            }

            if (minStep.node.isLeaf()) {
                RecordBuffer minRecordBuffer = ((BPTreeRecords) minStep.page).getRecordBuffer();
                int idxMin = minRecordBuffer.find(minRecord);
                idxMin = idxMin < 0 ? -idxMin - 1 : idxMin;

                RecordBuffer maxRecordBuffer = ((BPTreeRecords) maxStep.page).getRecordBuffer();
                int idxMax =  maxRecordBuffer.find(maxRecord);
                idxMax = idxMax < 0 ? -idxMax - 1 : idxMax;

                if (equalsStep(minStep, maxStep)) {
                    cardinality = idxMax - idxMin;
                } else {
                    cardinality += minRecordBuffer.size() - idxMin;
                    cardinality += idxMax;
                }
            }
        }

        return cardinality;
    }

    /**
     * `random()` modifies the behavior of the iterator so that each
     * `next()` provides a new random binding within the interval.
     * Beware that it does not terminate nor ensure distinct bindings.
     *
     * As for `cardinality()`, it uses the underlying balanced tree to
     * efficiently reach a Record between two access paths.
     *
     * @return True if the random can return a solution, false otherwise.
     */
    @Override
    public boolean random() {
        // computing random steps from the root to a leaf
        AccessPath randomPath = randomWalk();

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

    /**
     * Convenience function that checks the equality of two access paths.
     **/
    private static boolean equalsStep(AccessPath.AccessStep o1, AccessPath.AccessStep o2) {
        return o1.node.getId() == o2.node.getId() &&
            o1.idx == o2.idx &&
            o1.page.getId() == o2.page.getId();
    }

}
