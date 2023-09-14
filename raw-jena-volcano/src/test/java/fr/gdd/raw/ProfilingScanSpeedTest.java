package fr.gdd.raw;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.tdb2.TDB2Factory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * To help profiling a basic execution and evaluate the efficiency
 * of scans under concurrent calls. They are expected to be highly efficient since they
 * are read-only. However, as of 2023-05-06, they were not… A lock exists upon creation
 * which preclude the massive creation of scan iterators.
 */
public class ProfilingScanSpeedTest {

    private static Logger log = LoggerFactory.getLogger(ProfilingScanSpeedTest.class);

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
                Context c = dataset.getContext().copy().set(RAWConstants.timeout, TIMEOUT);
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
}
