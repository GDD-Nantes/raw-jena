package org.apache.jena.dboe.trans.bplustree;

import fr.gdd.raw.OpExecutorRAW;
import fr.gdd.raw.QueryEngineRAW;
import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.databases.inmemory.InMemoryInstanceOfTDB2ForRandom;
import fr.gdd.sage.databases.inmemory.SmallBlocksInMemoryTDB2ForCardinality;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.iterator.RAWJenaIteratorWrapper;
import org.apache.jena.sparql.engine.iterator.RAWScanIteratorFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.tdb2.solver.BindingNodeId;
import org.apache.jena.tdb2.solver.PreemptStageMatchTuple;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RAWJenaIteratorCardinalityTest {

    private static final Logger log = LoggerFactory.getLogger(RAWJenaIteratorCardinalityTest.class);

    static Dataset dataset;
    static Dataset smallerDataset;

    @BeforeAll
    public static void initializeDB() throws NoSuchFieldException, IllegalAccessException {
        dataset = new InMemoryInstanceOfTDB2ForRandom().getDataset();
        smallerDataset = new SmallBlocksInMemoryTDB2ForCardinality().getDataset();
        QueryEngineRAW.register();
    }

    @AfterAll
    public static void closeDB() {
        dataset.abort();
        TDBInternal.expel(dataset.asDatasetGraph());
    }

    @Test
    public void cardinality_of_fully_bounded_existing_triple() {
        OpBGP op = (OpBGP) SSE.parseOp("(bgp (<http://Alice> <http://own> <http://cat>))");
        RAWJenaIteratorWrapper it = getRandomJenaIterator(op, dataset);
        assertEquals(1, it.cardinality());
    }

    @Test
    public void cardinality_of_fully_bounded_missing_triple() {
        OpBGP op = (OpBGP) SSE.parseOp("(bgp (<http://Alice> <http://own> <http://licorne>))");
        RAWJenaIteratorWrapper it = getRandomJenaIterator(op, dataset);
        assertEquals(0, it.cardinality());
    }

    @Test
    public void cardinality_of_nothing_but_not_fully_bounded_triple() {
        OpBGP op = (OpBGP) SSE.parseOp("(bgp (?s ?p <http://licorne>))");
        RAWJenaIteratorWrapper it = getRandomJenaIterator(op, dataset);
        assertEquals(0, it.cardinality());
    }

    @Test
    public void cardinality_of_a_one_tuple_triple_pattern() {
        OpBGP op = (OpBGP) SSE.parseOp("(bgp (?s ?p <http://cat>))");
        RAWJenaIteratorWrapper it = getRandomJenaIterator(op, dataset);
        assertEquals(1, it.cardinality());
    }

    @Test
    public void cardinality_of_a_few_tuples_triple_pattern() {
        OpBGP op = (OpBGP) SSE.parseOp("(bgp (<http://Alice> ?p ?o))");
        RAWJenaIteratorWrapper it = getRandomJenaIterator(op, dataset);
        assertEquals(4, it.cardinality());
    }

    @Test
    public void cardinality_of_larger_triple_pattern_above_leaf_size() {
        ProgressJenaIterator.NB_WALKS = 1000; // we want a precise estimate :p
        OpBGP op = (OpBGP) SSE.parseOp("(bgp (<http://Alice> ?p ?o))");
        RAWJenaIteratorWrapper it = getRandomJenaIterator(op, smallerDataset);
        log.debug("Got estimation of {} but actually its 50.", it.cardinality());
        assertTrue(40 <= it.cardinality() &&  it.cardinality() <= 50); // ± 10 (but we have no guarantee on accuracy…)
    }


    /* ********************************************************************* */

    public static RAWJenaIteratorWrapper getRandomJenaIterator(OpBGP op, Dataset dataset) {
        // rough copy from {@link PreemptStageMatchTuple}
        DatasetGraphTDB activeGraph = TDBInternal.getDatasetGraphTDB(dataset);

        ExecutionContext execCxt = new ExecutionContext(
                dataset.getContext(),
                dataset.asDatasetGraph().getDefaultGraph(),
                activeGraph,
                new OpExecutorRAW.OpExecutorRandomFactory(dataset.getContext()));

        Tuple<Node>  patternTuple = TupleFactory.create3(
                op.getPattern().get(0).getSubject(),
                op.getPattern().get(0).getPredicate(),
                op.getPattern().get(0).getObject());

        NodeId[] ids = new NodeId[patternTuple.len()]; // ---- Convert to NodeIds
        final Var[] vars = new Var[patternTuple.len()]; // Variables for this tuple after substitution

        NodeTupleTable nodeTupleTable = activeGraph.getTripleTable().getNodeTupleTable();
        boolean found = PreemptStageMatchTuple.prepare(nodeTupleTable.getNodeTable(), patternTuple, BindingNodeId.root, ids, vars);

        execCxt.getContext().set(SageConstants.iterators, new HashMap<>());
        RAWScanIteratorFactory f = new RAWScanIteratorFactory(execCxt);
        if (found) {
            return (RAWJenaIteratorWrapper) f.getScan(nodeTupleTable, TupleFactory.create(ids), vars, 12);
        } else {
            return (RAWJenaIteratorWrapper) f.getScan(12);
        }
    }


}