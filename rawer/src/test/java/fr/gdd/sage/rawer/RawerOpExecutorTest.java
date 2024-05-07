package fr.gdd.sage.rawer;

import fr.gdd.sage.databases.inmemory.InMemoryInstanceOfTDB2ForRandom;
import fr.gdd.sage.jena.JenaBackend;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class RawerOpExecutorTest {

    private static final Logger log = LoggerFactory.getLogger(RawerOpExecutorTest.class);
    private static final InMemoryInstanceOfTDB2ForRandom dataset = new InMemoryInstanceOfTDB2ForRandom();

    @Test
    public void simple_triple_pattern () { // as per usual
        String queryAsString = "SELECT * WHERE {?s ?p ?o}";
        execute(queryAsString, dataset.getDataset(), 10L);
    }

    @Test
    public void simple_project_on_a_triple_pattern () { // as per usual
        String queryAsString = "SELECT ?s WHERE {?s ?p ?o}";
        execute(queryAsString, dataset.getDataset(), 10L);
    }

    @Test
    public void simple_bind_on_a_triple_pattern () {
        String queryAsString = "SELECT * WHERE {BIND (<http://Alice> AS ?s) ?s ?p ?o}";
        execute(queryAsString, dataset.getDataset(), 10L);
    }

    @Test
    public void count_of_simple_triple_pattern () {
        String queryAsString = "SELECT (COUNT(*) AS ?c) WHERE {?s ?p ?o}";
        execute(queryAsString, dataset.getDataset(), 10L);
    }

    @Test
    public void count_with_group_on_simple_tp () {
        String queryAsString = "SELECT (COUNT(*) AS ?c) ?p WHERE {?s ?p ?o} GROUP BY ?p";
        execute(queryAsString, dataset.getDataset(), 10L);
    }

    @Test
    public void count_distinct_of_simple_triple_pattern () {
        String queryAsString = "SELECT (COUNT(DISTINCT *) AS ?c) WHERE {?s ?p ?o}";
        execute(queryAsString, dataset.getDataset(), 10L);
    }

    /* ************************************************************* */

    public static void execute(String queryAsString, Dataset dataset, Long limit) {
        Op query = Algebra.compile(QueryFactory.create(queryAsString));

        ExecutionContext ec = new ExecutionContext(dataset.asDatasetGraph());
        ec.getContext().set(RawerConstants.BACKEND, new JenaBackend(dataset));
        ARQ.enableOptimizer(false);
        RawerOpExecutor executor = new RawerOpExecutor(ec).setLimit(limit);

        // QueryIterator iterator = executor.execute(query);
        Iterator iterator = executor.execute(query);

        int nbResults = 0;
        while (iterator.hasNext()) {
            log.debug("{}", iterator.next());
            nbResults += 1;
        }
        assertEquals(limit, nbResults);
    }

}