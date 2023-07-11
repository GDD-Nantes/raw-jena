package fr.gdd.sage;

import fr.gdd.sage.arq.SageConstants;
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

    private static Logger log = LoggerFactory.getLogger(OpExecutorRAWNLJTest.class);

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
        Op op = SSE.parseOp("(join (bgp (?s <http://address> ?o)) (bgp (?s <http://own> ?a)))");
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        // set ARQ.optimization to false in order to disable the merge of BGPs
        Context c = dataset.getContext().copy().set(SageConstants.limit, 1).set(ARQ.optimization, false);
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
        Op op = SSE.parseOp("(join (bgp (?s <http://address> ?o)) (bgp (?s <http://own> ?a)))");
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        final long LIMIT = 1000;
        // set ARQ.optimization to false in order to disable the merge of BGPs
        Context c = dataset.getContext().copy().set(SageConstants.limit, LIMIT).set(ARQ.optimization, false);
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
        Op op = SSE.parseOp("(join (bgp (?s <http://address> ?o)) (bgp (?s ?l <http://nowhere>)))");
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        final long LIMIT = 1;
        final long TIMEOUT = 100;
        Context c = dataset.getContext().copy().set(SageConstants.limit, LIMIT).set(SageConstants.timeout, TIMEOUT).set(ARQ.optimization, false);
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
        Op op = SSE.parseOp("(join (bgp (?a <http://species> <http://canine>)) (join (bgp (?s <http://address> ?o)) (bgp (?s <http://own> ?a))))");
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        final Long LIMIT = 1000L;
        Context c = dataset.getContext().copy().set(RAWConstants.limit, LIMIT).set(ARQ.optimization, false);
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