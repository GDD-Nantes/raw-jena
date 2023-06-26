package org.apache.jena.sparql.engine.iterator;

import fr.gdd.sage.RAWConstants;
import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.io.RAWInput;
import fr.gdd.sage.io.SageInput;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.main.QC;

import java.util.Objects;


/**
 * An iterator placed near the root that always produce new results until stopping a stopping
 * condition is met. For instance, it reached the maximum number of random walks.
 **/
public class RAWCounterIter extends QueryIterRepeatApply {

    Integer nbResults = 0;
    Integer nbScans = 0; // (TODO) put a limit on that too, number of scans maximal reached

    RAWInput input;
    Op op;
    Binding initialBinding;
    QueryIterator current;

    public RAWCounterIter(Op op, Binding initialBinding, ExecutionContext context) {
        super(QueryIterRoot.create(context), context); // `QueryIterRoot.create` to avoid complaints of super.
        input = context.getContext().get(RAWConstants.input);
        this.initialBinding = initialBinding;
        this.op = op;
        this.nextStage(initialBinding);
    }

    @Override
    protected QueryIterator nextStage(Binding binding) {
        if (Objects.nonNull(current)) {
            performClose(current);
        }
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
        return input.limitReached(nbResults) || input.deadlineReached();
    }

    @Override
    protected boolean hasNextBinding() {
        while (!current.hasNext() && !stoppingCondition()) {
            nextStage(initialBinding);
        }

        return !stoppingCondition();
    }

    @Override
    protected Binding moveToNextBinding() {
        nbResults += 1;
        return current.next();
    }

    @Override
    protected void requestSubCancel() {/* nothing */}

    @Override
    protected void closeSubIterator() {
        if (Objects.nonNull(current)) {
            performClose(current);
        }
    }
}


