package fr.gdd.sage;

import fr.gdd.sage.databases.inmemory.InMemoryInstanceOfTDB2ForRandom;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.tdb2.solver.OpExecutorTDB2;
import org.apache.jena.tdb2.solver.QueryEngineTDB;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpExecutorRAWBGPTest {

    private static Logger log = LoggerFactory.getLogger(OpExecutorRAWBGPTest.class);

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
    public void get_random_from_spo() {
        Op op = SSE.parseOp("(bgp (?s ?p ?o))");
        Set<Binding> allBindings = generateResults(op, dataset);

        Context c = dataset.getContext().copy().set(RAWConstants.limitRWs, 1L);
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        long sum = 0;
        while (iterator.hasNext()) {
            assertTrue(allBindings.contains(iterator.next()));
            sum += 1;
        }
        assertEquals(1, sum);
    }

    @Test
    public void get_a_random_from_a_triple_pattern() {
        Op op = SSE.parseOp("(bgp (?s <http://address> ?o))");
        Set<Binding> allBindings = generateResults(op, dataset);

        Context c = dataset.getContext().copy().set(RAWConstants.limitRWs, 1L);
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        long sum = 0;
        while (iterator.hasNext()) {
            assertTrue(allBindings.contains(iterator.next()));
            sum += 1;
        }
        assertEquals(1, sum);
    }

    @Test
    public void get_a_random_singleton() {
        Op op = SSE.parseOp("(bgp (?p <http://own> <http://dog>))");
        Set<Binding> allBindings = generateResults(op, dataset);

        Context c = dataset.getContext().copy().set(RAWConstants.limitRWs, 1L);
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        long sum = 0;
        while (iterator.hasNext()) {
            assertTrue(allBindings.contains(iterator.next()));
            sum += 1;
        }
        assertEquals(1, sum);
    }

    @Test
    public void get_1000_randoms_from_a_triple_pattern() {
        Op op = SSE.parseOp("(bgp (?s <http://address> ?o))");
        Set<Binding> allBindings = generateResults(op, dataset);

        final long LIMIT = 1000L;
        Context c = dataset.getContext().copy().set(RAWConstants.limitRWs, LIMIT);
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        long sum = 0;
        Set<Binding> randomSetOfBindings = new HashSet<>();
        while (iterator.hasNext()) {
            Binding randomBinding = iterator.next();
            assertTrue(allBindings.contains(randomBinding));
            randomSetOfBindings.add(randomBinding);
            sum += 1;
        }
        assertEquals(LIMIT, sum);
        assertEquals(allBindings.size(), randomSetOfBindings.size());
        for (Binding existingBinding : allBindings) {
            randomSetOfBindings.contains(existingBinding);
        }
    }

    @Test
    public void get_a_random_from_a_bgp_of_two_triple_patterns() {
        String queryAsString = "(bgp (?s <http://address> ?o) (?s <http://own> ?a))";
        Op op = SSE.parseOp(queryAsString);
        Set<Binding> allBindings = generateResults(op, dataset);

        Context c = dataset.getContext().copy().set(RAWConstants.limitRWs, 1000L);
        long LIMIT = 1; // only want 1 successful walk
        op = SSE.parseOp(String.format("(slice _ %s %s)", LIMIT, queryAsString));
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        long sum = 0;
        while (iterator.hasNext()) {
            Binding b = iterator.next();
            assertTrue(allBindings.contains(b));
            sum += 1;
        }
        assertEquals(1, sum);
    }

    @Test
    public void get_1000_randoms_from_a_bgp_of_two_triple_patterns() {
        String queryAsString = "(bgp (?s <http://address> ?o) (?s <http://own> ?a))";
        Op op = SSE.parseOp(queryAsString);
        Set<Binding> allBindings = generateResults(op, dataset);

        final long LIMIT = 1000; // 1000 successful walks
        final long TIMEOUT = 60000; // long timeout of 60s for only 1k random walks
        Context c = dataset.getContext().copy().set(RAWConstants.timeout, TIMEOUT);

        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        op = SSE.parseOp(String.format("(slice _ %s %s)", LIMIT, queryAsString));
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);
        QueryIterator iterator = plan.iterator();
        long sum = 0;
        Set<Binding> randomSetOfBindings = new HashSet<>();
        while (iterator.hasNext()) {
            Binding randomBinding = iterator.next();
            assertTrue(allBindings.contains(randomBinding));
            randomSetOfBindings.add(randomBinding);
            sum += 1;
        }
        assertEquals(LIMIT, sum);
        assertEquals(allBindings.size(), randomSetOfBindings.size());
        for (Binding existingBinding : allBindings) {
            randomSetOfBindings.contains(existingBinding);
        }
    }

    @Test
    public void get_1000_randoms_but_timeout_is_too_small_so_we_get_less() throws InterruptedException {
        String queryAsString = "(bgp (?s <http://address> ?o) (?s <http://own> ?a))";
        Op op = SSE.parseOp(queryAsString);
        Set<Binding> allBindings = generateResults(op, dataset);

        final long LIMIT = 10000; // successful walks
        final long TIMEOUT = 100; // ms, still too small to go through
        Context c = dataset.getContext().copy().set(RAWConstants.timeout, TIMEOUT);
        op = SSE.parseOp(String.format("(slice _ %s %s)", LIMIT, queryAsString));
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        long sum = 0;
        Set<Binding> randomSetOfBindings = new HashSet<>();
        while (iterator.hasNext()) {
            Binding randomBinding = iterator.next();
            assertTrue(allBindings.contains(randomBinding));
            randomSetOfBindings.add(randomBinding);
            Thread.sleep(Duration.ofMillis(1)); // delay even more, to make sure
            sum += 1;
        }

        log.info("Got {} successful walks in 100ms", sum);
        assertTrue(sum <= LIMIT); // cannot have more results than the timeout
    }

    @Test
    public void get_randoms_but_the_triple_pattern_has_no_results() {
        String queryAsString = "(bgp (?s <http://wrong_predicate> ?o))";
        Op op = SSE.parseOp(queryAsString);
        Set<Binding> allBindings = generateResults(op, dataset);
        assertEquals(0, allBindings.size());

        final long LIMIT = 1; // successful walks limit will never be reached
        final long TIMEOUT = 100; // ms
        long startExecution = System.currentTimeMillis();
        Context c = dataset.getContext().copy().set(RAWConstants.timeout, TIMEOUT);
        op = SSE.parseOp(String.format("(slice _ %s %s)", LIMIT, queryAsString));
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        long sum = 0;
        Set<Binding> randomSetOfBindings = new HashSet<>();
        while (iterator.hasNext()) {
            Binding randomBinding = iterator.next();
            assertTrue(allBindings.contains(randomBinding));
            randomSetOfBindings.add(randomBinding);
            sum += 1;
        }

        long elapsedExecution = System.currentTimeMillis() - startExecution;

        log.info("Took {}ms to get {} results.", elapsedExecution, sum);
        assertTrue(elapsedExecution >= TIMEOUT); // it stopped because of timeout
        assertEquals(0, randomSetOfBindings.size());
        assertEquals(0, sum); // no loop turn.
    }

    @Test
    public void get_randoms_from_a_triple_pattern_that_has_one_result() {
        String queryAsString = "(bgp (?s ?p <http://cat>))";
        Op op = SSE.parseOp(queryAsString);
        Set<Binding> allBindings = generateResults(op, dataset);
        assertEquals(1, allBindings.size());

        long startExecution = System.currentTimeMillis();
        final long LIMIT = 10;
        Context c = dataset.getContext().copy().set(RAWConstants.timeout, 60000L);
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        op = SSE.parseOp(String.format("(slice _ %s %s)", LIMIT, queryAsString));
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        long sum = 0;
        Set<Binding> randomSetOfBindings = new HashSet<>();
        while (iterator.hasNext()) {
            Binding randomBinding = iterator.next();
            assertTrue(allBindings.contains(randomBinding));
            randomSetOfBindings.add(randomBinding);
            sum += 1;
        }
        long elapsedExecution = System.currentTimeMillis() - startExecution;

        assertEquals(1, randomSetOfBindings.size());
        log.info("Took {}ms to get {} times the same result.", elapsedExecution, sum);
        assertEquals(LIMIT, sum); // 10 times the same
    }


    /**
     * Generates all bindings of an operation in order to check if random results belong to it.
     * @param op The operation to execute.
     * @return A set of bindings.
     */
    static Set<Binding> generateResults(Op op, Dataset dataset) {
        // must erase factory for dataset…
        QC.setFactory(dataset.getContext(), OpExecutorTDB2.OpExecFactoryTDB);
        Plan planTDB = QueryEngineTDB.getFactory().create(op, dataset.asDatasetGraph(), BindingRoot.create(), dataset.getContext().copy());
        HashSet<Binding> bindings = new HashSet<>();
        QueryIterator iteratorTDB = planTDB.iterator();
        while (iteratorTDB.hasNext()) {
            bindings.add(iteratorTDB.next());
        }
        return bindings;
    }



}