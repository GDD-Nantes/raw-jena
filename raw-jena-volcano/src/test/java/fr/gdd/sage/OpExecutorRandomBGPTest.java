package fr.gdd.sage;

import fr.gdd.sage.arq.SageConstants;
import fr.gdd.sage.databases.inmemory.InMemoryInstanceOfTDB2ForRandom;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
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
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.solver.OpExecutorTDB2;
import org.apache.jena.tdb2.solver.QueryEngineTDB;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpExecutorRandomBGPTest {

    private static Logger log = LoggerFactory.getLogger(OpExecutorRandomBGPTest.class);

    static Dataset dataset;

    @BeforeAll
    public static void initializeDB() {
        dataset = new InMemoryInstanceOfTDB2ForRandom().getDataset();
        QueryEngineRandom.register();
    }

    @AfterAll
    public static void closeDB() {
        dataset.abort();
        TDBInternal.expel(dataset.asDatasetGraph());
    }

    @Disabled
    @Test
    public void get_random_from_spo() {
        Op op = SSE.parseOp("(bgp (?s ?p ?o))");
        Set<Binding> allBindings = generateResults(op, dataset);

        Context c = dataset.getContext().copy().set(SageConstants.limit, 1);
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

        Context c = dataset.getContext().copy().set(SageConstants.limit, 1);
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

        final long LIMIT = 1000;
        Context c = dataset.getContext().copy().set(SageConstants.limit, LIMIT);
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
        Op op = SSE.parseOp("(bgp (?s <http://address> ?o) (?s <http://own> ?a))");
        Set<Binding> allBindings = generateResults(op, dataset);


        Context c = dataset.getContext().copy().set(SageConstants.limit, 1);
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
    public void get_1000_randoms_from_a_bgp_of_two_triple_patterns() {
        Op op = SSE.parseOp("(bgp (?s <http://address> ?o) (?s <http://own> ?a))");
        Set<Binding> allBindings = generateResults(op, dataset);

        final long LIMIT = 1000;
        Context c = dataset.getContext().copy().set(SageConstants.limit, LIMIT);
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
    public void get_1000_randoms_but_timeout_is_too_small_so_we_get_less() throws InterruptedException {
        Op op = SSE.parseOp("(bgp (?s <http://address> ?o) (?s <http://own> ?a))");
        Set<Binding> allBindings = generateResults(op, dataset);

        final long LIMIT = 1000;
        final long TIMEOUT = 100; // ms
        Context c = dataset.getContext().copy().set(SageConstants.limit, LIMIT).set(SageConstants.timeout, TIMEOUT);
        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

        QueryIterator iterator = plan.iterator();
        long sum = 0;
        Set<Binding> randomSetOfBindings = new HashSet<>();
        while (iterator.hasNext()) {
            Binding randomBinding = iterator.next();
            assertTrue(allBindings.contains(randomBinding));
            randomSetOfBindings.add(randomBinding);
            Thread.sleep(Duration.ofMillis(1));
            sum += 1;
        }
        assertTrue(sum <= TIMEOUT); // cannot have more results than the timeout
    }

    @Test
    public void get_randoms_but_the_triple_pattern_has_no_results() {
        Op op = SSE.parseOp("(bgp (?s <http://wrong_predicate> ?o))");
        Set<Binding> allBindings = generateResults(op, dataset);
        assertEquals(0, allBindings.size());

        final long LIMIT = 1000;
        final long TIMEOUT = 100; // ms
        long startExecution = System.currentTimeMillis();
        Context c = dataset.getContext().copy().set(SageConstants.limit, LIMIT).set(SageConstants.timeout, TIMEOUT);
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
        assertTrue(elapsedExecution >= TIMEOUT); // it stopped because of timeout
        assertEquals(0, randomSetOfBindings.size());
        assertEquals(0, sum); // no loop turn.
    }


    @Test
    public void get_randoms_from_a_triple_pattern_that_has_one_result() {
        Op op = SSE.parseOp("(bgp (?s ?p <http://cat>))");
        Set<Binding> allBindings = generateResults(op, dataset);
        assertEquals(1, allBindings.size());

        final long LIMIT = 10;
        Context c = dataset.getContext().copy().set(SageConstants.limit, LIMIT);
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

        assertEquals(1, randomSetOfBindings.size());
        assertEquals(LIMIT, sum); // no loop turn.
    }

    @Disabled
    @Test
    public void test_concurrent_execution_to_profile_perf() throws InterruptedException {
        Dataset dataset = TDB2Factory.connectDataset("../target/watdiv10M");

        int numberOfThreads = 1;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        Op op = SSE.parseOp("(bgp (?s <http://db.uwaterloo.ca/~galuc/wsdbm/gender> ?o))");

        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                // final long LIMIT = 10000;
                final long TIMEOUT = 10000;

                dataset.begin(ReadWrite.READ);
                // Context c = dataset.getContext().copy().set(SageConstants.limit, LIMIT);
                Context c = dataset.getContext().copy().set(SageConstants.timeout, TIMEOUT);
                QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), c);
                Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), c);

                QueryIterator iterator = plan.iterator();
                long sum = 0;
                Set<Binding> randomSetOfBindings = new HashSet<>();
                while (iterator.hasNext()) {
                    Binding randomBinding = iterator.next();
                    randomSetOfBindings.add(randomBinding);
                    sum += 1;
                }
                // assertEquals(LIMIT, sum);

                log.info("Number of random walks performed by a thread: {}", sum);
                latch.countDown();
            });
        }
        latch.await();
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