package fr.gdd.sage.io;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.sse.SSE;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Just making sure that it generates proper JSON.
 */
class OpSerializeJSONTest {

    @Disabled
    @Test
    public void serialize_bgp () {
        Op op = SSE.parseOp("(bgp (?s ?p ?o))");
        OpSerializeJSON osj = new OpSerializeJSON(op);
        System.out.println(osj.result);
        // (TODO) more serious serialization of plan, then tests
    }

}