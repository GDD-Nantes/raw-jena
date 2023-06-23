package fr.gdd.sage;

import fr.gdd.sage.arq.OpExecutorSage;
import fr.gdd.sage.arq.SageConstants;
import org.apache.jena.sparql.engine.iterator.ScanIteratorFactory;
import fr.gdd.sage.configuration.SageServerConfiguration;
import org.apache.jena.sparql.algebra.op.OpConditional;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.*;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.util.Context;

import java.util.Objects;

/**
 * Executes a random branch of the tree that represents a query.
 */
public class OpExecutorRAW extends OpExecutorSage {

    public static class OpExecutorRandomFactory implements OpExecutorFactory {
        SageServerConfiguration configuration;

        public OpExecutorRandomFactory(Context context) {
            configuration = new SageServerConfiguration(context);
        }

        @Override
        public OpExecutor create(ExecutionContext context) {
            return new OpExecutorRAW(context, configuration);
        }
    }

    public OpExecutorRAW(ExecutionContext context, SageServerConfiguration configuration) {
        super(context, configuration);
        ScanIteratorFactory scanFactory = context.getContext().get(SageConstants.scanFactory);
        if (Objects.isNull(scanFactory) || !(scanFactory instanceof RAWScanIteratorFactory)) {
            // since it inherits from Sage, it may be already set to preemptScanIteratorFactory, so we reset
            context.getContext().set(SageConstants.scanFactory, new RAWScanIteratorFactory(context));
        }
    }

    @Override
    public QueryIterator execute(OpUnion opUnion, QueryIterator input) {
        return new RAWQueryIterUnion(input, flattenUnion(opUnion), execCxt);
    }

    @Override
    protected QueryIterator execute(OpJoin opJoin, QueryIterator input) {
        return new RAWQueryIterNestedLoopJoin(opJoin, input, execCxt);
    }

    @Override
    protected QueryIterator execute(OpConditional opCondition, QueryIterator input) {
        return new RAWQueryIterOptionalIndex(exec(opCondition.getLeft(), input), opCondition.getRight(), execCxt);
    }
}
