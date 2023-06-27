package org.apache.jena.sparql.engine.iterator;

import fr.gdd.sage.RAWConstants;
import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.io.RAWInput;
import fr.gdd.sage.io.RAWOutput;
import fr.gdd.sage.io.SageInput;
import org.apache.jena.dboe.trans.bplustree.RAWJenaIterator;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.util.Context;

import java.util.HashMap;
import java.util.Objects;


/**
 * An iterator placed near the root that always produce new results until stopping a stopping
 * condition is met. For instance, it reached the maximum number of random walks.
 **/
public class RAWCounterIter extends QueryIterRepeatApply {

    Integer nbResults = 0;
    Integer nbScans = 0; // (TODO) put a limit on that too, number of scans maximal reached

    RAWInput input;
    RAWOutput output;
    Op op;
    Binding initialBinding;
    QueryIterator current;

    public RAWCounterIter(Op op, Binding initialBinding, ExecutionContext context) {
        super(QueryIterRoot.create(context), context); // `QueryIterRoot.create` to avoid complaints of super.
        // input = context.getContext().get(RAWConstants.input);
        output = context.getContext().get(RAWConstants.output);
        this.initialBinding = initialBinding;
        this.op = op;
        this.nextStage(initialBinding);
    }

    @Override
    protected QueryIterator nextStage(Binding binding) {
        if (Objects.nonNull(current)) { performClose(current); }
        QueryIterator qIter1 = (binding.isEmpty()) ? QueryIterRoot.create(getExecContext())
                        : QueryIterRoot.create(binding, getExecContext());
        current = QC.execute(op, qIter1, getExecContext());
        return current;
    }

    /**
     * Evaluate if the iterator should stop looking for new random walks.
     * @return True if it should stop, false otherwise.
     */
    private boolean stoppingCondition() {
        input = Objects.isNull(input) ? getExecContext().getContext().get(RAWConstants.input) : input;
        return input.limitReached(nbResults) || input.deadlineReached();
    }

    @Override
    protected boolean hasNextBinding() {
        while (!current.hasNext() && !stoppingCondition()) {
            nextStage(initialBinding);
        }

        if (stoppingCondition()) {
            throw new PauseException();
        }

        return true;
    }

    @Override
    protected Binding moveToNextBinding() {
        nbResults += 1;

        HashMap<Integer, RAWJenaIteratorWrapper> iterators = getExecContext().getContext().get(SageConstants.iterators);
        output.addResultThenClear(iterators);

        return current.next();
    }

    @Override
    protected void requestSubCancel() {
        if (Objects.nonNull(current)) {
            performRequestCancel(current);
        }
    }

    @Override
    protected void closeSubIterator() {
        if (Objects.nonNull(current)) {
            performClose(current);
        }
    }
}


