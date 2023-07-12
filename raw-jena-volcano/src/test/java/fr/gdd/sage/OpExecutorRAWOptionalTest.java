package fr.gdd.sage;

import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.databases.inmemory.InMemoryInstanceOfTDB2ForRandom;
import org.apache.jena.ext.com.google.common.collect.HashMultiset;
import org.apache.jena.ext.com.google.common.collect.Multiset;
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

import static org.junit.jupiter.api.Assertions.*;

class OpExecutorRAWOptionalTest {

    private static Logger log = LoggerFactory.getLogger(OpExecutorRAWOptionalTest.class);

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
    public void get_a_random_from_an_option() {
        Op op = SSE.parseOp("(conditional (bgp (?s <http://address> ?o)) (bgp (?s <http://own> ?a)))");
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        Context c = dataset.getContext().copy().set(RAWConstants.limitRWs, 1L);
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

        log.debug("Random result from optional: {}", result);
        assertEquals(1, sum);
    }


    @Test
    public void get_1000_randoms_from_an_option() {
        Op op = SSE.parseOp("(conditional (bgp (?s <http://address> ?o)) (bgp (?s <http://own> ?a)))");
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        final long LIMIT = 1000;
        Context c = dataset.getContext().copy().set(SageConstants.limit, LIMIT);
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

        // It should highlight a skew: Of course, mandatory parts without optional parts
        // appear more often than mandatory+optional since the random is divided between
        // the sub path that are optional.
        for (Binding binding : randomSetOfBindings.elementSet()) {
            log.debug("{} -> {}", binding, randomSetOfBindings.count(binding));
        }
    }

    @Test
    public void get_1000_random_from_optional_that_succeed_and_fail_in_option() {
        Op op = SSE.parseOp("(conditional (bgp (?s <http://address> ?o)) (bgp (?s <http://own> ?a) (?a <http://species> <http://canine>)))");
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        final long LIMIT = 1000;
        Context c = dataset.getContext().copy().set(SageConstants.limit, LIMIT);
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        Multiset<Binding> randomSetOfBindings = HashMultiset.create();
        while (iterator.hasNext()) {
            Binding randomBinding = iterator.next();
            // cannot assert since mandatory alone is sometime not in the results set
            // assertTrue(allBindings.contains(randomBinding));
            randomSetOfBindings.add(randomBinding);
        }
        assertEquals(LIMIT, randomSetOfBindings.size());
        // not equal since for some results there are (mandatory and mandatory+option)
        assertNotEquals(allBindings.size(), randomSetOfBindings.elementSet().size());
        for (Binding existingBinding : allBindings) {
            randomSetOfBindings.contains(existingBinding);
        }

        // It should highlight a skew: Of course, mandatory parts without optional parts
        // appear more often than mandatory+optional since the random is divided between
        // the sub path that are optional.
        for (Binding binding : randomSetOfBindings.elementSet()) {
            log.debug("{} -> {}", binding, randomSetOfBindings.count(binding));
        }
    }

    @Test
    public void mandatory_part_of_optional_is_empty() {
        Op op = SSE.parseOp("(conditional (bgp (?s <http://nothing> ?o)) (bgp (?s <http://own> ?a)))");

        final long LIMIT = 100;
        final long TIMEOUT = 100; // 1s
        Context c = dataset.getContext().copy().set(SageConstants.limit, LIMIT).set(SageConstants.timeout, TIMEOUT);
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        Multiset<Binding> randomSetOfBindings = HashMultiset.create();
        while (iterator.hasNext()) {
            Binding randomBinding = iterator.next();
            // cannot assert since mandatory alone is sometime not in the results set
            // assertTrue(allBindings.contains(randomBinding));
            randomSetOfBindings.add(randomBinding);
        }
        assertEquals(0, randomSetOfBindings.size());
    }

}