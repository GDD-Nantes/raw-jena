package fr.gdd.fedup;

import fr.gdd.sage.QueryEngineRandom;
import fr.gdd.sage.databases.inmemory.InMemoryInstanceOfTDB2;
import fr.gdd.sage.databases.inmemory.InMemoryInstanceOfTDB2ForRandom;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FedUPExplorerTest {

    static Dataset dataset;

    @BeforeAll
    public static void initializeDB() {
        dataset = new InMemoryInstanceOfTDB2().getDataset();
        QueryEngineRandom.register();
    }

    @AfterAll
    public static void closeDB() {
        dataset.abort();
        TDBInternal.expel(dataset.asDatasetGraph());
    }

    @Disabled
    @Test
    public void simple_test_of_explorer() {
        Op op = SSE.parseOp("(bgp (?s <http://www.geonames.org/ontology#parentCountry> ?o) (?s ?p <http://db.uwaterloo.ca/~galuc/wsdbm/Country2>))");
        FedUPExplorer fue = new FedUPExplorer(dataset);
        fue.build(op);
    }

}