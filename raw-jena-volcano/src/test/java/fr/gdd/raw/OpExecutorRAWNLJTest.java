package fr.gdd.raw;

import fr.gdd.raw.io.RAWInput;
import fr.gdd.sage.databases.inmemory.InMemoryInstanceOfTDB2ForRandom;
import org.apache.jena.ext.com.google.common.collect.HashMultiset;
import org.apache.jena.ext.com.google.common.collect.Multiset;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpExecutorRAWNLJTest {

    private static final Logger log = LoggerFactory.getLogger(OpExecutorRAWNLJTest.class);

    static Dataset dataset;

    @BeforeAll
    public static void initializeDB() {
        dataset = new InMemoryInstanceOfTDB2ForRandom().getDataset();
        QueryEngineRAW.register();
    }

    @AfterAll
    public static void closeDB() {
        dataset.abort();
        TDBInternal.expel(dataset.asDatasetGraph());
    }

    @Test
    public void get_a_random_from_join() {
        String queryAsString = "(join (bgp (?s <http://address> ?o)) (bgp (?s <http://own> ?a)))";
        Op op = SSE.parseOp(queryAsString);
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        // set ARQ.optimization to false in order to disable the merge of BGPs
        long LIMIT = 1;
        Context c = dataset.getContext().copy().set(RAWConstants.timeout, 10000L)
                .set(ARQ.optimization, false)
                .set(RAWConstants.input, new RAWInput());
        op = SSE.parseOp(String.format("(slice _ %s %s)", LIMIT, queryAsString));
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        long sum = 0;
        Binding result = null;
        while (iterator.hasNext()) {
            result = iterator.next();
            assertTrue(allBindings.contains(result));
            sum += 1;
        }
        log.debug("Random result found: {}", result);
        assertEquals(1, sum);
    }

    @Test
    public void get_1000_randoms_from_a_join_of_bgp() {
        String queryAsString = "(join (bgp (?s <http://address> ?o)) (bgp (?s <http://own> ?a)))";
        Op op = SSE.parseOp(queryAsString);
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        final long LIMIT = 1000;
        // set ARQ.optimization to false in order to disable the merge of BGPs
        Context c = dataset.getContext().copy().set(RAWConstants.timeout, 10000L)
                .set(ARQ.optimization, false)
                .set(RAWConstants.input, new RAWInput());
        op = SSE.parseOp(String.format("(slice _ %s %s)", LIMIT, queryAsString));
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        Multiset<Binding> randomSetOfBindings = HashMultiset.create();
        while (iterator.hasNext()) {
            Binding randomBinding = iterator.next();
            assertTrue(allBindings.contains(randomBinding));
            randomSetOfBindings.add(randomBinding);
        }
        assertEquals(LIMIT, randomSetOfBindings.size());
        assertEquals(allBindings.size(), randomSetOfBindings.elementSet().size());
        for (Binding existingBinding : allBindings) {
            randomSetOfBindings.contains(existingBinding);
        }

        for (Binding binding : randomSetOfBindings.elementSet()) {
            log.debug("{} -> {}", binding, randomSetOfBindings.count(binding));
        }
    }

    @Test
    public void get_a_random_from_join_without_results() {
        String queryAsString = "(join (bgp (?s <http://address> ?o)) (bgp (?s ?l <http://nowhere>)))";
        Op op = SSE.parseOp(queryAsString);
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        final long LIMIT = 1;
        final long TIMEOUT = 1000;
        op = SSE.parseOp(String.format("(slice _ %s %s)", LIMIT, queryAsString));
        Context c = dataset.getContext().copy()
                .set(RAWConstants.timeout, TIMEOUT)
                .set(ARQ.optimization, false)
                .set(RAWConstants.input, new RAWInput());
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);

        long startExecution = System.currentTimeMillis();
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        Multiset<Binding> randomSetOfBindings = HashMultiset.create();

        while (iterator.hasNext()) {
            Binding randomBinding = iterator.next();
            assertTrue(allBindings.contains(randomBinding));
            randomSetOfBindings.add(randomBinding);
        }
        long elapsedExecution = System.currentTimeMillis() - startExecution;

        assertEquals(0, randomSetOfBindings.size());
        assertTrue(elapsedExecution >= TIMEOUT);
    }

    @Test
    public void join_of_a_join() {
        String queryAsString = "(join (bgp (?a <http://species> <http://canine>)) (join (bgp (?s <http://address> ?o)) (bgp (?s <http://own> ?a))))";
        Op op = SSE.parseOp(queryAsString);
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        final Long LIMIT = 10L; // there is one result that we get 10 times easily below 2s time mark.
        Context c = dataset.getContext().copy()
                .set(RAWConstants.timeout, 2000L)
                .set(ARQ.optimization, false)
                .set(RAWConstants.input, new RAWInput());
        op = SSE.parseOp(String.format("(slice _ %s %s)", LIMIT, queryAsString));
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);

        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        Multiset<Binding> randomSetOfBindings = HashMultiset.create();

        while (iterator.hasNext()) {
            Binding randomBinding = iterator.next();
            assertTrue(allBindings.contains(randomBinding));
            randomSetOfBindings.add(randomBinding);
        }

        assertEquals(LIMIT, randomSetOfBindings.size());
        assertEquals(allBindings.size(), randomSetOfBindings.elementSet().size());

        for (Binding binding : randomSetOfBindings.elementSet()) {
            log.debug("{} -> {}", binding, randomSetOfBindings.count(binding));
        }
    }

}