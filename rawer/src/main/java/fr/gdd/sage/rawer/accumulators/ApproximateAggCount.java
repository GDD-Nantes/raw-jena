package fr.gdd.sage.rawer.accumulators;

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
public class ApproximateAggCount extends AggCount {

    final ExecutionContext context;
    final Op op;

    public ApproximateAggCount(ExecutionContext context, Op subOp) {
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


        public ApproximateAccCount(ExecutionContext context, Op subOp) {
            this.context = context;
            this.op = subOp;
        }

        @Override
        public void accumulate(Binding binding, FunctionEnv functionEnv) {
            // TODO
        }

        @Override
        public NodeValue getValue() {
            return NodeValue.makeDouble(numberOfRWs/sumOfProba);
        }
    }
}
