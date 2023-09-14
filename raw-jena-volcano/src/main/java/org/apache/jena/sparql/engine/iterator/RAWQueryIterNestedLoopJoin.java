package org.apache.jena.sparql.engine.iterator;

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
public class RAWQueryIterNestedLoopJoin extends QueryIter1 {

    boolean isFirstExecution = true;

    Binding left;
    Binding right;
    QueryIterator leftIterator;
    QueryIterator rightIterator;

    public RAWQueryIterNestedLoopJoin(OpJoin opJoin, QueryIterator input, ExecutionContext context) {
        super(input, context);
        leftIterator = QC.execute(opJoin.getLeft(), input.nextBinding(), getExecContext());
        left = leftIterator.hasNext() ? leftIterator.next() : null;
        rightIterator = QC.execute(opJoin.getRight(), left, getExecContext());
        right = rightIterator.hasNext() ? rightIterator.next() : null;
    }

    @Override
    protected boolean hasNextBinding() {
        return isFirstExecution &&
                Objects.nonNull(left) &&
                Objects.nonNull(right);
    }

    @Override
    protected Binding moveToNextBinding() {
        isFirstExecution = false;
        return right;
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
