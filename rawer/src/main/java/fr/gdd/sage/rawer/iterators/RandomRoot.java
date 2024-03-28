package fr.gdd.sage.rawer.iterators;

import fr.gdd.jena.visitors.ReturningArgsOpVisitorRouter;
import fr.gdd.sage.rawer.RawerConstants;
import fr.gdd.sage.rawer.RawerOpExecutor;
import fr.gdd.sage.sager.BindingId2Value;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.ExecutionContext;

import java.util.Iterator;
import java.util.Objects;

public class RandomRoot implements Iterator<BindingId2Value> {

    final Long limit;
    final Long deadline;
    final Op op;
    final RawerOpExecutor executor;

    Long count = 0L;
    Iterator<BindingId2Value> current;

    public RandomRoot(RawerOpExecutor executor, ExecutionContext context, Op op) {
        this.limit = context.getContext().get(RawerConstants.LIMIT, Long.MAX_VALUE);
        this.deadline = context.getContext().get(RawerConstants.DEADLINE, Long.MAX_VALUE);
        this.executor = executor;
        this.op = op;
    }

    @Override
    public boolean hasNext() {
        current = null;
        while (Objects.isNull(current) || !current.hasNext()) {
            current = ReturningArgsOpVisitorRouter.visit(this.executor, this.op, Iter.of(new BindingId2Value()));
            if (shouldStop()) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldStop() {
        return System.currentTimeMillis() > deadline || count >= limit;
    }

    @Override
    public BindingId2Value next() {
        ++count;
        return current.next();
    }
}
