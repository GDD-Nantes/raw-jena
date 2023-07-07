package org.apache.jena.sparql.engine.iterator;

import fr.gdd.sage.RAWConstants;
import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.io.RAWInput;
import fr.gdd.sage.io.RAWOutput;
import fr.gdd.sage.io.RAWOutputAggregated;
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

    /**
     * The number of random walks from the root until it fails or succeed.
     */
    Integer nbWalks = 0;

    /**
     * The number of valid results, ie., random walks that succeed.
     */
    Integer nbResults = 0;

    RAWInput input;
    RAWOutput output;
    RAWOutputAggregated outputAggregated;
    Op op;
    Binding initialBinding;
    QueryIterator current;

    public RAWCounterIter(Op op, Binding initialBinding, ExecutionContext context) {
        super(QueryIterRoot.create(context), context); // `QueryIterRoot.create` to avoid complaints of super.
        output = context.getContext().get(RAWConstants.output);
        outputAggregated = context.getContext().get(RAWConstants.outputAggregated);
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
        return input.limitReached(nbWalks) || input.deadlineReached();
    }

    @Override
    protected boolean hasNextBinding() {

        while (!stoppingCondition() && !current.hasNext()) {
            getExecContext().getContext().set(SageConstants.cursor, 0);
            HashMap<Integer, RAWJenaIteratorWrapper> iterators = getExecContext().getContext().get(SageConstants.iterators);
            outputAggregated.addResult(iterators);
            output.addResultThenClear(iterators);
            nextStage(initialBinding);
            nbWalks += 1;
        }

        if (stoppingCondition()) {
            throw new PauseException();
        }

        return true;
    }

    @Override
    protected Binding moveToNextBinding() {
        nbResults += 1; // got an actual result
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


