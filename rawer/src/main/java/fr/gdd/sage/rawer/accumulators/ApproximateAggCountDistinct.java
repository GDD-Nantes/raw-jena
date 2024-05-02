package fr.gdd.sage.rawer.accumulators;

import fr.gdd.jena.visitors.ReturningOpVisitorRouter;
import fr.gdd.sage.sager.SagerConstants;
import fr.gdd.sage.sager.pause.Save2SPARQL;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.Accumulator;
import org.apache.jena.sparql.expr.aggregate.AggCount;
import org.apache.jena.sparql.function.FunctionEnv;

/**
 * Perform an estimate of the COUNT based on random walks performed on
 * the subQuery.
 */
public class ApproximateAggCountDistinct extends AggCount {

    final ExecutionContext context;
    final Op op;

    public ApproximateAggCountDistinct(ExecutionContext context, Op subOp) {
        this.context = context;
        this.op = subOp;
    }

    @Override
    public Accumulator createAccumulator() {
        return new ApproximateAccCount(context, op);
    }

    /* ******************************************************************** */

    public static class ApproximateAccCount implements Accumulator {

        final ExecutionContext context;
        final Op op;

        Double numberOfRWs = 0.;
        Double sumOfProba = 0.;

        WanderJoinVisitor wj;

        public ApproximateAccCount(ExecutionContext context, Op subOp) {
            this.context = context;
            this.op = subOp;
            Save2SPARQL saver = context.getContext().get(SagerConstants.SAVER);
            this.wj = new WanderJoinVisitor(saver.getOp2it());
        }

        @Override
        public void accumulate(Binding binding, FunctionEnv functionEnv) {
            sumOfProba += 1./ReturningOpVisitorRouter.visit(wj, op);
            numberOfRWs += 1;
        }

        @Override
        public NodeValue getValue() {
            return NodeValue.makeDouble(sumOfProba == 0. ? 0. : sumOfProba/numberOfRWs);
        }
    }
}
