package org.apache.jena.sparql.engine.iterator;

import fr.gdd.sage.RAWConstants;
import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.io.RAWInput;
import fr.gdd.sage.io.RAWOutput;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.dboe.trans.bplustree.RAWJenaIterator;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.util.iterator.NullIterator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

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
    Var[] vars;


    public RAWJenaIteratorWrapper(Iterator<Tuple<NodeId>> wrapped, Integer id, Var[] vars, NodeTupleTable nodeTupleTable, ExecutionContext context) {
        this.wrapped = wrapped;
        this.input = context.getContext().get(RAWConstants.input);
        this.output = context.getContext().get(RAWConstants.output);
        this.context = context;
        this.id = id;
        this.ntt = nodeTupleTable;
        this.vars = vars;

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

    public Binding getCurrent() {
        BindingBuilder result = Binding.builder();
        if (wrapped instanceof NullIterator) {
            return result.build(); // nothing
        }

        Tuple<NodeId> nodeIds = ((RAWJenaIterator) wrapped).getCurrent();

        if (Objects.isNull(nodeIds)) {
            return result.build();
        }

        for (int i = 0; i < vars.length; ++i) {
            if (Objects.nonNull(vars[i])) {
                result.add(vars[i], ntt.getNodeTable().getNodeForNodeId(nodeIds.get(i)));
            }
        }
        return result.build();
    }

    public long getCardinality() {
        if (wrapped instanceof NullIterator) {
            return 0L;
        } else {
            return ((RAWJenaIterator) wrapped).cardinality();
        }
    }
}
