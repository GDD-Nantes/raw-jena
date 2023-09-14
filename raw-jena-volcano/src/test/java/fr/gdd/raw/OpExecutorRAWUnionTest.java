package fr.gdd.raw;

import fr.gdd.raw.io.RAWInput;
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
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpExecutorRAWUnionTest {

    private static Logger log = LoggerFactory.getLogger(OpExecutorRAWUnionTest.class);

    static Dataset dataset;

    @BeforeAll
    public static void initializeDB() {
        dataset = new InMemoryInstanceOfTDB2ForRandom().getDataset();
        dataset.getContext().set(RAWConstants.timeout, 1000L).set(RAWConstants.input, new RAWInput());
        QueryEngineRAW.register();
    }

    @AfterAll
    public static void closeDB() {
        dataset.abort();
        TDBInternal.expel(dataset.asDatasetGraph());
    }

    @Test
    public void get_a_random_from_union() {
        String queryAsString = "(union (bgp (?s <http://address> ?o)) (bgp (?s <http://own> ?a)))";
        Op op = SSE.parseOp(queryAsString);
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        long LIMIT = 1;
        op = SSE.parseOp(String.format("(slice _ %s %s)", LIMIT, queryAsString));
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), dataset.getContext());
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), dataset.getContext());

        QueryIterator iterator = plan.iterator();
        long sum = 0;
        while (iterator.hasNext()) {
            assertTrue(allBindings.contains(iterator.next()));
            sum += 1;
        }
        assertEquals(1, sum);
    }

    @Test
    public void get_1000_randoms_from_a_union_of_bgp() {
        String queryAsString = "(union (bgp (?s <http://address> ?o)) (bgp (?s <http://own> ?a)))";
        Op op = SSE.parseOp(queryAsString);
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        final long LIMIT = 1000;
        op = SSE.parseOp(String.format("(slice _ %s %s)", LIMIT, queryAsString));
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), dataset.getContext());
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), dataset.getContext());

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
    public void get_1000_randoms_from_a_union_inside_bgp() {
        String queryAsString = "(join (bgp (?a <http://species> ?s)) (union (bgp (?n <http://address> ?o)) (bgp (?n <http://own> ?a))))";
        Op op = SSE.parseOp(queryAsString);
        Set<Binding> allBindings = OpExecutorRAWBGPTest.generateResults(op, dataset);

        final long LIMIT = 1000;
        op = SSE.parseOp(String.format("(slice _ %s %s)", LIMIT, queryAsString));
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), dataset.getContext());
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), dataset.getContext());

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

}