package fr.gdd.raw;

import fr.gdd.raw.io.RAWInput;
import fr.gdd.sage.databases.inmemory.InMemoryInstanceOfTDB2ForRandom;
import org.apache.jena.ext.com.google.common.collect.HashMultiset;
import org.apache.jena.ext.com.google.common.collect.Multiset;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.tdb2.sys.TDBInternal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpExecutorRAWValuesTest {

    private static Logger log = LoggerFactory.getLogger(OpExecutorRAWValuesTest.class);

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
    public void simple_values_that_excludes_a_value_from_being_picked() {
        String queryAsString = """
            SELECT * WHERE {
                VALUES ?s {<http://Alice> <http://Carol>}
                ?s <http://address> ?o
            } LIMIT 1000""";
        Query query = QueryFactory.create(queryAsString);
        Op op = Algebra.compile(query);

        QueryEngineFactory factory = QueryEngineRegistry.findFactory(op, dataset.asDatasetGraph(), dataset.getContext());
        Plan plan = factory.create(op, dataset.asDatasetGraph(), BindingRoot.create(), dataset.getContext());

        QueryIterator iterator = plan.iterator();
        Multiset<Binding> randomSetOfBindings = HashMultiset.create();
        while (iterator.hasNext()) {
            Binding randomBinding = iterator.next();
            randomSetOfBindings.add(randomBinding);
        }
        assertEquals(1000, randomSetOfBindings.size());
        for (Binding b : randomSetOfBindings.elementSet()) {
            log.debug(String.format("Found %s x %s", randomSetOfBindings.count(b), b.toString()));
        }
        assertEquals(2, randomSetOfBindings.elementSet().size());
    }
}
