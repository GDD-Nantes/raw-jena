package org.apache.jena.sparql.engine.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.io.SageInput;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QC;

/**
 * Unions create an iterator that concatenate operation. We want this
 * iterator to be randomized, therefore, each `nextStage` randomizes
 * the list of operations. 
 **/
public class RAWQueryIterUnion extends QueryIter1 {

    List<Op> initialOps;

    QueryIterator current;

    SageInput<?> input;

    boolean isFirstExecution = true;
    Binding initialBinding;

    public RAWQueryIterUnion(QueryIterator input,
                             List<Op> subOps,
                             ExecutionContext context) {
        super(input, context);
        initialOps = new ArrayList<>(subOps);
        this.input = context.getContext().get(SageConstants.input);
        initialBinding = getInput().next();

        Collections.shuffle(initialOps); // each new instance get a different order
        Op subOp = QC.substitute(initialOps.get(0), initialBinding) ;
        QueryIterator parent = QueryIterSingleton.create(initialBinding, getExecContext()) ;
        current = QC.execute(subOp, parent, getExecContext()) ;
    }

    @Override
    protected boolean hasNextBinding() {
        if (!isFirstExecution || System.currentTimeMillis() >= input.getDeadline()) {
            return false;
        }

        return current.hasNext();
    }

    @Override
    protected Binding moveToNextBinding() {
        isFirstExecution = false;

        return current.next();
    }

    @Override
    protected void requestSubCancel() {
        performRequestCancel(current);
    }

    @Override
    protected void closeSubIterator() {
        performClose(current);
    }
}
