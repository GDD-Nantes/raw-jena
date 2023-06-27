package fr.gdd.sage;

import fr.gdd.sage.arq.IdentifierLinker;
import fr.gdd.sage.arq.QueryEngineSage;
import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.io.RAWInput;
import fr.gdd.sage.io.RAWOutput;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpLib;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.*;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.*;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.mgt.Explain;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.solver.QueryEngineTDB;
import org.apache.jena.tdb2.store.DatasetGraphTDB;

public class QueryEngineRAW extends QueryEngineTDB {

    protected QueryEngineRAW(Op op, DatasetGraphTDB dataset, Binding input, Context context) {
        super(op, dataset, input, context);
    }

    protected QueryEngineRAW(Query query, DatasetGraphTDB dataset, Binding input, Context context) {
        super(query, dataset, input, context);
    }

    static public void register() {
        QueryEngineRegistry.addFactory(factory);
    }

    static public void unregister() {
        QueryEngineRegistry.removeFactory(factory);
    }


    private static boolean isUnionDefaultGraph(Context cxt) {
        return cxt.isTrue(TDB2.symUnionDefaultGraph1) || cxt.isTrue(TDB2.symUnionDefaultGraph2);
    }

    @Override
    public QueryIterator eval(Op op, DatasetGraph dsg, Binding input, Context context) {
        // #1 an explain that comes from {@link QueryEngineTDB}
        if ( isUnionDefaultGraph(context) && !isDynamicDataset() ) {
            op = OpLib.unionDefaultGraphQuads(op) ;
            Explain.explain("REWRITE(Union default graph)", op, context);
        }

        if (op instanceof OpSlice) {
            // we get the min of all limits
            RAWInput rawInput = context.get(RAWConstants.input);
            rawInput.setLimit(Math.min(rawInput.limit, ((OpSlice) op).getLength()));
        }

        // #2 comes from {@link QueryEngineBase}
        ExecutionContext execCxt = new ExecutionContext(context, dsg.getDefaultGraph(), dsg,
                new OpExecutorRAW.OpExecutorRandomFactory(context));

        execCxt.getContext().setIfUndef(RAWConstants.output, new RAWOutput());
        execCxt.getContext().set(RAWConstants.input, new RAWInput(execCxt.getContext()));

        IdentifierLinker.create(execCxt, op, true);

        RAWCounterIter ci = new RAWCounterIter(op, input, execCxt);

        // Wrap with something to check for closed iterators.
        QueryIterator cIter = QueryIteratorCheck.check(ci, execCxt) ;
        // Need call back.
        if (context.isTrue(ARQ.enableExecutionTimeLogging))
            cIter = QueryIteratorTiming.time(cIter);



        return new PreemptRootIter((QueryIterator)cIter, execCxt);
    }

    /* ******************** Factory ********************** */
    public static QueryEngineFactory factory = new QueryEngineRandomFactory();

    /**
     * Mostly identical to {@link org.apache.jena.tdb2.solver.QueryEngineTDB.QueryEngineFactoryTDB}
     * but calling {@link QueryEngineRAW} instead of {@link QueryEngineTDB} to build plans.
     */
    public static class QueryEngineRandomFactory extends QueryEngineFactoryTDB {

        @Override
        public Plan create(Query query, DatasetGraph dataset, Binding input, Context context) {
            QueryEngineBase engine = new QueryEngineRAW(query, dsgToQuery(dataset), input, context);
            return engine.getPlan();
        }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding binding, Context context) {
            QueryEngineBase engine = new QueryEngineRAW(op, dsgToQuery(dataset), binding, context);
            return engine.getPlan();
        }

        @Override
        public boolean accept(Op op, DatasetGraph dataset, Context context) {
            // (TODO) onlySELECT ?
            return isNotInfiniteRandomWalking(context) && super.accept(op, dataset, context);
        }

        @Override
        public boolean accept(Query query, DatasetGraph dataset, Context context) {
            return onlySELECT(query) && isNotInfiniteRandomWalking(context) && super.accept(query, dataset, context);
        }

        private static boolean isNotInfiniteRandomWalking(Context context) {
            return context.isDefined(RAWConstants.limit) || context.isDefined(RAWConstants.timeout);
        }

        private static boolean onlySELECT(Query query) {
            return query.isSelectType();
        }
    }
}
