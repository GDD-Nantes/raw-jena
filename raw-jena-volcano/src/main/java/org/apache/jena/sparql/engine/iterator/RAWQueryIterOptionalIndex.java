package org.apache.jena.sparql.engine.iterator;

import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.io.SageInput;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QC;

import java.util.Objects;

/**
 * An iterator that randomly searches the optional part, and if there is a result
 * merge it to the input, otherwise, replace it by its default.
 *
 * It is worth noting
 * that this iterator may return the mandatory part without the optional part even
 * if some optional exists.
 */
public class RAWQueryIterOptionalIndex extends QueryIter1 {

    QueryIterator rightIterator;

    boolean isFirstExecution = true;
    SageInput<?> input;
    Binding mandatory;

    public RAWQueryIterOptionalIndex(QueryIterator input, Op rightOp, ExecutionContext execCxt) {
        super(input, execCxt);
        if (input.hasNext()) {
            mandatory = input.nextBinding();
            rightIterator = QC.execute(rightOp, mandatory, execCxt);
        }
        this.input = execCxt.getContext().get(SageConstants.input);
    }

    @Override
    protected boolean hasNextBinding() {
        if (!isFirstExecution || System.currentTimeMillis() >= input.getDeadline()) {
            return false;
        }

        return Objects.nonNull(mandatory); // as soon as the mandatory part is ok, the rest is ok
    }

    @Override
    protected Binding moveToNextBinding() {
        isFirstExecution = false;

        if (rightIterator.hasNext()) {
            return rightIterator.next();
        } else {
            return mandatory;
        }
    }

    @Override
    protected void requestSubCancel() {
        performRequestCancel(rightIterator);
    }

    @Override
    protected void closeSubIterator() {
        performClose(rightIterator);
    }
}
