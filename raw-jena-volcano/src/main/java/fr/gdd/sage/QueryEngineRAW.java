package fr.gdd.sage;

import fr.gdd.sage.arq.QueryEngineSage;
import fr.gdd.sage.arq.SageConstants;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpLib;
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

public class QueryEngineRAW extends QueryEngineSage {

    protected QueryEngineRAW(Op op, DatasetGraphTDB dataset, Binding input, Context context) {
        super(op, dataset, input, context);
        QC.setFactory(dataset.getContext(), new OpExecutorRAW.OpExecutorRandomFactory(context));
    }

    protected QueryEngineRAW(Query query, DatasetGraphTDB dataset, Binding input, Context context) {
        super(query, dataset, input, context);
        QC.setFactory(dataset.getContext(), new OpExecutorRAW.OpExecutorRandomFactory(context));
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

        // #2 comes from {@link QueryEngineBase}
        ExecutionContext execCxt = new ExecutionContext(context, dsg.getDefaultGraph(), dsg,
                new OpExecutorRAW.OpExecutorRandomFactory(context));

        /*
        if (execCxt.getContext().isUndef(SageConstants.input)) { // <=> setIfUndef
            // (TODO) improve , should this be here? should this have Sage ref' removed ?
            long limit = execCxt.getContext().getLong(SageConstants.limit, Long.MAX_VALUE);
            // always have a timeout otherwise could be infinite looping. Here arbitrarily set to 60s.
            long timeout = execCxt.getContext().getLong(SageConstants.timeout, 60000);
            SageInput<?> sageInput = new SageInput<>().setLimit(limit).setTimeout(timeout);

            execCxt.getContext().set(SageConstants.input, sageInput);
        } */

        // #3 inbetween we add our home-made counter iterator :)
        RAWCounterIter counterIter = new RAWCounterIter(op, input, execCxt);

        // Wrap with something to check for closed iterators.
        QueryIterator qIter = QueryIteratorCheck.check(counterIter, execCxt) ;
        // Need call back.
        if ( context.isTrue(ARQ.enableExecutionTimeLogging) )
            qIter = QueryIteratorTiming.time(qIter) ;
        return qIter ;
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
            QueryEngineSage engine = new QueryEngineRAW(query, dsgToQuery(dataset), input, context);
            return engine.getPlan();
        }

        @Override
        public Plan create(Op op, DatasetGraph dataset, Binding binding, Context context) {
            QueryEngineSage engine = new QueryEngineRAW(op, dsgToQuery(dataset), binding, context);
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
            return context.isDefined(SageConstants.limit) || context.isDefined(SageConstants.timeout);
        }

        private static boolean onlySELECT(Query query) {
            return query.isSelectType();
        }
    }
}
