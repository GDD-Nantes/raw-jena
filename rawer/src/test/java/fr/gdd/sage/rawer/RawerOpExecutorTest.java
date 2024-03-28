package fr.gdd.sage.rawer;

import fr.gdd.sage.databases.inmemory.InMemoryInstanceOfTDB2ForRandom;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class RawerOpExecutorTest {

    private static final Logger log = LoggerFactory.getLogger(RawerOpExecutorTest.class);
    private static final InMemoryInstanceOfTDB2ForRandom dataset = new InMemoryInstanceOfTDB2ForRandom();


    @Test
    public void simple_triple_pattern () { // as per usual
        String queryAsString = "SELECT * WHERE {?s ?p ?o}";
        Op query = Algebra.compile(QueryFactory.create(queryAsString));

        ExecutionContext ec = new ExecutionContext(dataset.getDataset().asDatasetGraph());
        ARQ.enableOptimizer(false);
        RawerOpExecutor executor = new RawerOpExecutor(ec).setLimit(10L);

        QueryIterator iterator = executor.execute(query);

        int nbResults = 0;
        while (iterator.hasNext()) {
            log.debug("{}", iterator.next());
            nbResults += 1;
        }
        assertEquals(10, nbResults);
    }

    @Test
    public void simple_project_on_a_triple_pattern () { // as per usual
        String queryAsString = "SELECT ?s WHERE {?s ?p ?o}";
        Op query = Algebra.compile(QueryFactory.create(queryAsString));

        ExecutionContext ec = new ExecutionContext(dataset.getDataset().asDatasetGraph());
        ARQ.enableOptimizer(false);
        RawerOpExecutor executor = new RawerOpExecutor(ec).setLimit(10L);

        QueryIterator iterator = executor.execute(query);

        int nbResults = 0;
        while (iterator.hasNext()) {
            log.debug("{}", iterator.next());
            nbResults += 1;
        }
        assertEquals(10, nbResults);
    }

    @Test
    public void count_of_simple_triple_pattern () {
        String queryAsString = "SELECT (COUNT(*) AS ?c) WHERE {?s ?p ?o}";
        Op query = Algebra.compile(QueryFactory.create(queryAsString));

        ExecutionContext ec = new ExecutionContext(dataset.getDataset().asDatasetGraph());
        ARQ.enableOptimizer(false);
        RawerOpExecutor executor = new RawerOpExecutor(ec).setLimit(10L);

        QueryIterator iterator = executor.execute(query);

        int nbResults = 0;
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
            nbResults += 1;
        }
        assertEquals(10, nbResults);
    }


}