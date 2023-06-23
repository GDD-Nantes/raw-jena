package fr.gdd.sage;

import fr.gdd.sage.databases.inmemory.InMemoryInstanceOfTDB2ForRandom;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryEngineRAWTest {

    static Dataset dataset;

    @BeforeAll
    public static void initializeDB() {
        dataset = new InMemoryInstanceOfTDB2ForRandom().getDataset();
    }

    @AfterAll
    public static void closeDB() {
        dataset.abort();
        TDBInternal.expel(dataset.asDatasetGraph());
    }


    @Test
    public void create_a_dataset_and_register_random_engine_then_check_if_called() {
        QueryEngineRAW.register();

        Op op = SSE.parseOp("(union " +
                "(bgp (?s ?p <http://db.uwaterloo.ca/~galuc/wsdbm/Country1>)) " +
                "(bgp (<http://db.uwaterloo.ca/~galuc/wsdbm/City0> <http://www.geonames.org/ontology#parentCountry> ?o))" +
                ")");

        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), dataset.getContext());
        assertTrue(factory instanceof QueryEngineRAW.QueryEngineRandomFactory);
    }

}