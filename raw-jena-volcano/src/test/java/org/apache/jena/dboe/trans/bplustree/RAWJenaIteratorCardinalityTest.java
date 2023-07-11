package org.apache.jena.dboe.trans.bplustree;

import fr.gdd.sage.OpExecutorRAW;
import fr.gdd.sage.QueryEngineRAW;
import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.databases.inmemory.InMemoryInstanceOfTDB2ForRandom;
import fr.gdd.sage.databases.inmemory.SmallBlocksInMemoryTDB2ForCardinality;
import fr.gdd.sage.databases.persistent.Watdiv10M;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.iterator.RAWJenaIteratorWrapper;
import org.apache.jena.sparql.engine.iterator.RAWScanIteratorFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.solver.BindingNodeId;
import org.apache.jena.tdb2.solver.PreemptStageMatchTuple;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RAWJenaIteratorCardinalityTest {

    Logger log = LoggerFactory.getLogger(RAWJenaIteratorCardinalityTest.class);

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

    @Disabled
    @Test
    public void cardinality_of_fully_bounded_missing_triple() {
        OpBGP op = (OpBGP) SSE.parseOp("(bgp (<http://Alice> <http://own> <http://licorne>))");
        RAWJenaIteratorWrapper it = getRandomJenaIterator(op, dataset);
        assertEquals(0, it.cardinality());
    }


    @Disabled
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

    @Disabled
    @Test
    public void cardinality_of_larger_triple_pattern_above_leaf_size() {
        OpBGP op = (OpBGP) SSE.parseOp("(bgp (<http://Alice> ?p ?o))");
        RAWJenaIteratorWrapper it = getRandomJenaIterator(op, smallerDataset);
        assertEquals(50, it.cardinality());
    }


    @Test
    @EnabledIfEnvironmentVariable(named = "WATDIV", matches = "true")
    public void cardinality_of_larger_triple_pattern_above_leaf_size_with_watdiv() {
        Watdiv10M watdiv10M = new Watdiv10M(Optional.of("../target"));
        Dataset watdiv = TDB2Factory.connectDataset(watdiv10M.dbPath_asStr);
        watdiv.begin(ReadWrite.READ);
        // OpBGP op = (OpBGP) SSE.parseOp("(bgp (?v0 <http://schema.org/eligibleRegion> <http://db.uwaterloo.ca/~galuc/wsdbm/Country21>))"); // expect 2613 get 2613
        // OpBGP op = (OpBGP) SSE.parseOp("(bgp (?v0 <http://purl.org/goodrelations/validThrough> ?v3))"); // expect 36346 get 34100
        // OpBGP op = (OpBGP) SSE.parseOp("(bgp (?v0 <http://purl.org/goodrelations/includes> ?v1))"); // expect 90000 get 103616
        // OpBGP op = (OpBGP) SSE.parseOp("(bgp (?v1 <http://schema.org/text> ?v6))"); // expect 7476 get 7476
        // OpBGP op = (OpBGP) SSE.parseOp("(bgp (?v0 <http://schema.org/eligibleQuantity> ?v4))"); // expect 90000 get 79454
        OpBGP op = (OpBGP) SSE.parseOp("(bgp (?v0 <http://purl.org/goodrelations/price> ?v2))"); // expect 240000 get 234057

        RAWJenaIteratorWrapper it = getRandomJenaIterator(op, watdiv);
        assertEquals(50, it.cardinality());
        watdiv.end();
    }


    public static RAWJenaIteratorWrapper getRandomJenaIterator(OpBGP op, Dataset dataset) {
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

        NodeId ids[] = new NodeId[patternTuple.len()]; // ---- Convert to NodeIds
        final Var[] vars = new Var[patternTuple.len()]; // Variables for this tuple after substitution

        NodeTupleTable nodeTupleTable = activeGraph.getTripleTable().getNodeTupleTable();
        PreemptStageMatchTuple.prepare(nodeTupleTable.getNodeTable(), patternTuple, BindingNodeId.root, ids, vars);

        execCxt.getContext().set(SageConstants.iterators, new HashMap());
        RAWScanIteratorFactory f = new RAWScanIteratorFactory(execCxt);
        return (RAWJenaIteratorWrapper) f.getScan(nodeTupleTable, TupleFactory.create(ids), vars, 12);
    }


}