package fr.gdd.sage.rawer.iterators;

import fr.gdd.jena.visitors.ReturningArgsOpVisitorRouter;
import fr.gdd.sage.generics.BackendBindings;
import fr.gdd.sage.rawer.RawerConstants;
import fr.gdd.sage.rawer.RawerOpExecutor;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.ExecutionContext;

import java.util.Iterator;
import java.util.Objects;

/**
 * Iterator in charge of checking some stopping conditions, e.g., the execution
 * time reached the timeout.
 */
public class RandomRoot<ID, VALUE> implements Iterator<BackendBindings<ID, VALUE>> {

    final Long limit;
    final Long deadline;
    final Op op;
    final RawerOpExecutor<ID, VALUE> executor;
    final ExecutionContext context;

    Long count = 0L;
    Iterator<BackendBindings<ID, VALUE>> current;
    BackendBindings<ID, VALUE> produced;

    public RandomRoot(RawerOpExecutor<ID, VALUE> executor, ExecutionContext context, Op op) {
        this.limit = context.getContext().get(RawerConstants.LIMIT, Long.MAX_VALUE);
        this.deadline = context.getContext().get(RawerConstants.DEADLINE, Long.MAX_VALUE);
        this.executor = executor;
        this.op = op;
        this.context = context;
    }

    @Override
    public boolean hasNext() {
        while (Objects.isNull(produced)) {
            if (shouldStop()) {
                return false;
            }
            // TODO input as Iterator<BindingId2Value>
            if (Objects.nonNull(current) && current.hasNext()) {
                produced = current.next();
            } else {
                current = ReturningArgsOpVisitorRouter.visit(this.executor, this.op, Iter.of(new BackendBindings<>()));
            }
        }
        return true;
    }

    private boolean shouldStop() {
        return System.currentTimeMillis() > deadline ||
                count >= limit ||
                context.getContext().getLong(RawerConstants.SCANS, 0L) >= limit;
    }

    @Override
    public BackendBindings<ID, VALUE> next() {
        ++count;
        BackendBindings<ID, VALUE> toReturn = produced; // ugly :(
        produced = null;
        return toReturn;
    }
}
