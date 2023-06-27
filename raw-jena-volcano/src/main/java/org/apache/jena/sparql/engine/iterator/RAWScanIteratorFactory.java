package org.apache.jena.sparql.engine.iterator;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.dboe.trans.bplustree.PreemptTupleIndexRecord;
import org.apache.jena.dboe.trans.bplustree.RAWJenaIterator;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.util.iterator.NullIterator;

import java.util.Iterator;
import java.util.Objects;

/**
 * Returns one-time-use iterators that randomly jump in their set of possible
 * results.
 */
public class RAWScanIteratorFactory extends PreemptScanIteratorFactory implements ScanIteratorFactory {

    public RAWScanIteratorFactory(ExecutionContext context) {
        super(context);
    }

    @Override
    public Iterator<Tuple<NodeId>> getScan(Integer id) {
        return new NullIterator<>();
    }

    @Override
    public Iterator<Quad> getScan(Tuple<NodeId> pattern, Integer id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Tuple<NodeId>> getScan(NodeTupleTable nodeTupleTable, Tuple<NodeId> pattern, Integer id) {
        PreemptTupleIndexRecord.IteratorBuilder builder = null;
        if (pattern.len() < 4) {
            PreemptTupleIndexRecord ptir = preemptTripleTupleTable.findIndex(pattern);
            builder = ptir.genericScan(pattern);
        } else {
            PreemptTupleIndexRecord ptir = preemptQuadTupleTable.findIndex(pattern);
            builder = ptir.genericScan(pattern);
        }

        Iterator<Tuple<NodeId>> wrapped = Objects.isNull(builder.ptir) ?
                new NullIterator<>():
                new RAWJenaIterator(builder.ptir, builder.min, builder.max);
        return new RAWJenaIteratorWrapper(wrapped, id, nodeTupleTable, context);
    }
}
