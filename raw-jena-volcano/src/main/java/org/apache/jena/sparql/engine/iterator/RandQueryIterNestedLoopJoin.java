package org.apache.jena.sparql.engine.iterator;

import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.io.SageInput;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QC;

import java.util.Objects;

/**
 * Similarly to random unions, random nested loop join operators takes
 * a random binding as input from its left hand side. Then tries to join
 * with a random binding from its right hand side.
 */
public class RandQueryIterNestedLoopJoin extends QueryIter1 {

    boolean isFirstExecution = true;

    Binding left;
    Binding right;
    QueryIterator leftIterator;
    QueryIterator rightIterator;

    SageInput<?> input;

    public RandQueryIterNestedLoopJoin(OpJoin opJoin, QueryIterator input, ExecutionContext context) {
        super(input, context);
        leftIterator = QC.execute(opJoin.getLeft(), QueryIterRoot.create(getExecContext()), context);
        left = leftIterator.hasNext() ? leftIterator.next() : null;
        rightIterator = QC.execute(opJoin.getRight(), input.nextBinding(), getExecContext());
        right = rightIterator.hasNext() ? rightIterator.next() : null;
        this.input = context.getContext().get(SageConstants.input);
    }

    @Override
    protected boolean hasNextBinding() {
        if (!isFirstExecution || System.currentTimeMillis() >= input.getDeadline()) {
            return false;
        }

        return Objects.nonNull(left) && Objects.nonNull(right) && Objects.nonNull(Algebra.merge(left, right));
    }

    @Override
    protected Binding moveToNextBinding() {
        isFirstExecution = false;

        return Algebra.merge(left, right);
    }


    @Override
    protected void requestSubCancel() {
        performRequestCancel(leftIterator);
        performRequestCancel(rightIterator);
    }

    @Override
    protected void closeSubIterator() {
        performClose(leftIterator);
        performClose(rightIterator);
    }
}
