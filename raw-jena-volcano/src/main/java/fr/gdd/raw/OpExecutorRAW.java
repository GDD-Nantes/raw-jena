package fr.gdd.raw;

import fr.gdd.sage.arq.SageConstants;
import fr.gdd.raw.io.RAWInput;
import fr.gdd.raw.io.RAWOutput;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.*;
import org.apache.jena.sparql.engine.join.Join;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.OpExecutorFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.tdb2.solver.PatternMatchSage;

import java.util.HashMap;

/**
 * Executes a random branch of the tree that represents a query.
 */
public class OpExecutorRAW extends OpExecutor {

    public static class OpExecutorRandomFactory implements OpExecutorFactory {
        RAWInput configuration;

        public OpExecutorRandomFactory(Context context) {
            configuration = new RAWInput(context);
        }

        @Override
        public OpExecutor create(ExecutionContext context) {
            return new OpExecutorRAW(context, new RAWInput(context.getContext()));
        }
    }

    public OpExecutorRAW(ExecutionContext context, RAWInput configuration) {
        super(context);
        this.execCxt.getContext().setIfUndef(RAWConstants.input, configuration);
        this.execCxt.getContext().setIfUndef(SageConstants.scanFactory, new RAWScanIteratorFactory(context));
        this.execCxt.getContext().setIfUndef(SageConstants.cursor, 0);
        this.execCxt.getContext().setIfUndef(SageConstants.iterators, new HashMap());
    }

    @Override
    public QueryIterator executeOp(Op op, QueryIterator input) {
        this.execCxt.getContext().setIfUndef(RAWConstants.output, new RAWOutput(op));
        return super.executeOp(op, input);
    }

    @Override
    protected QueryIterator execute(OpBGP opBGP, QueryIterator input) {
        return PatternMatchSage.matchTriplePattern(opBGP.getPattern(), input, execCxt);
    }

    @Override
    protected QueryIterator execute(OpTriple opTriple, QueryIterator input) {
        return PatternMatchSage.matchTriplePattern(opTriple.asBGP().getPattern(), input, execCxt);
    }

    @Override
    protected QueryIterator execute(OpQuadPattern quadPattern, QueryIterator input) {
        return PatternMatchSage.matchQuadPattern(quadPattern.getBasicPattern(), quadPattern.getGraphNode(), input, execCxt);
    }

    @Override
    protected QueryIterator execute(OpQuad opQuad, QueryIterator input) {
        return PatternMatchSage.matchQuadPattern(opQuad.asQuadPattern().getBasicPattern(), opQuad.getQuad().getGraph(), input, execCxt);
    }

    // UNION
    @Override
    public QueryIterator execute(OpUnion opUnion, QueryIterator input) {
        return new RAWQueryIterUnion(input, flattenUnion(opUnion), execCxt);
    }

    // JOIN
    @Override
    protected QueryIterator execute(OpJoin opJoin, QueryIterator input) {
        return new RAWQueryIterNestedLoopJoin(opJoin, input, execCxt);
    }

    // OPTIONAL
    @Override
    protected QueryIterator execute(OpConditional opCondition, QueryIterator input) {
        return new RAWQueryIterOptionalIndex(exec(opCondition.getLeft(), input), opCondition.getRight(), execCxt);
    }

    @Override
    protected QueryIterator execute(OpLeftJoin opLeftJoin, QueryIterator input) {
        return new RAWQueryIterOptionalIndex(exec(opLeftJoin.getLeft(), input), opLeftJoin.getRight(), execCxt);
    }

    // VALUES
    @Override
    protected QueryIterator execute(OpTable opTable, QueryIterator input) {
        if (opTable.isJoinIdentity()) { // nothing to do with the table
            return input;
        } else if (input.isJoinIdentity()) { // nothing to do with the input
            input.close();
            return new RAWTableIterator(opTable.getTable(), this.execCxt);
        } else { // otherwise join the table with the input
            QueryIterator qIterT = new RAWTableIterator(opTable.getTable(), this.execCxt);
            QueryIterator qIter = Join.join(input, qIterT, this.execCxt);
            return qIter;
        }
    }

    @Override
    protected QueryIterator execute(OpSequence opSequence, QueryIterator input) {
        return super.execute(opSequence, input); // nothing changes
    }
}
