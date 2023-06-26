package org.apache.jena.sparql.engine.iterator;

import fr.gdd.sage.RAWConstants;
import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.io.RAWInput;
import fr.gdd.sage.io.RAWOutput;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.tdb2.store.NodeId;

import java.util.Iterator;

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


    public RAWJenaIteratorWrapper(Iterator<Tuple<NodeId>> wrapped, Integer id, ExecutionContext context) {
        this.wrapped = wrapped;
        this.input = context.getContext().get(RAWConstants.input);
        this.output = context.getContext().get(RAWConstants.output);
        this.context = context;
        this.id = id;
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
}
