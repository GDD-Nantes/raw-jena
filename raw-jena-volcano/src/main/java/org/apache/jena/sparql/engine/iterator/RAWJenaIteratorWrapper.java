package org.apache.jena.sparql.engine.iterator;

import fr.gdd.sage.RAWConstants;
import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.io.RAWInput;
import fr.gdd.sage.io.RAWOutput;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.dboe.trans.bplustree.RAWJenaIterator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * Wraps the raw RAWJenaIterator with a layer that checks if stopping conditions are
 * met.
 */
public class RAWJenaIteratorWrapper implements Iterator<Tuple<NodeId>> {

    Iterator<Tuple<NodeId>> wrapped;
    Integer id;
    RAWInput input;
    RAWOutput output;
    ExecutionContext context;

    NodeTupleTable ntt;


    public RAWJenaIteratorWrapper(Iterator<Tuple<NodeId>> wrapped, Integer id, NodeTupleTable nodeTupleTable, ExecutionContext context) {
        this.wrapped = wrapped;
        this.input = context.getContext().get(RAWConstants.input);
        this.output = context.getContext().get(RAWConstants.output);
        this.context = context;
        this.id = id;
        this.ntt = nodeTupleTable;

        HashMap<Integer, RAWJenaIteratorWrapper> iterators = this.context.getContext().get(SageConstants.iterators);
        iterators.put(id, this);
    }


    @Override
    public boolean hasNext() {
        if (input.deadlineReached() || input.limitReached(output.getNbScans())) {
            throw new PauseException();
        }
        return wrapped.hasNext();
    }

    @Override
    public Tuple<NodeId> next() {
        output.addScan();
        this.context.getContext().set(SageConstants.cursor, this.id);
        return wrapped.next();
    }

    public Iterator<Tuple<NodeId>> getWrapped() {
        return wrapped;
    }

    public Tuple<Node> getCurrent() {
        Tuple<NodeId> nodeIds = ((RAWJenaIterator) wrapped).getCurrent();
        Tuple<Node> nodes = TupleFactory.create(nodeIds.stream().map(i -> ntt.getNodeTable().getNodeForNodeId(i)).collect(Collectors.toList()));
        return nodes;
    }
}
