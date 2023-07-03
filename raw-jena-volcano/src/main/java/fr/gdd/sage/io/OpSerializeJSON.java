package fr.gdd.sage.io;

import fr.gdd.sage.arq.IdentifierAllocator;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.*;

import java.util.stream.Collectors;

public class OpSerializeJSON extends OpVisitorUnimplemented {

    String result = "";
    IdentifierAllocator identifiers;

    public OpSerializeJSON(Op op) {
        this.identifiers = new IdentifierAllocator();
        op.visit(this.identifiers);
        op.visit(this);
        result = "[" + result + "]";
    }

    @Override
    public void visit(OpBGP opBGP) {
        result += Streams.zip(opBGP.getPattern().getList().stream(), this.identifiers.getIds(opBGP).stream(),
                        (t, i) -> String.format("{ \"op\": \"%s\", \"type\": \"triple\", \"id\": %s }", t, i)
                        ).collect(Collectors.joining(","));
    }

    @Override
    public void visit(OpQuadPattern opQuadPattern) {
        result += Streams.zip(opQuadPattern.getPattern().getList().stream(), this.identifiers.getIds(opQuadPattern).stream(),
                (q, i) -> String.format("{ \"op\": \"%s %s\", \"type\": \"quad\", \"id\": %s }", opQuadPattern.getGraphNode().toString(), q, i))
                .collect(Collectors.joining(","));
    }

    @Override
    public void visit(OpTriple opTriple) {
        result += String.format("{ \"op\": \"%s\", \"type\": \"triple\", \"id\": %s }", opTriple.getTriple(), identifiers.getIds(opTriple).get(0));
    }

    @Override
    public void visit(OpQuad opQuad) {
        result += String.format("{ \"op\": \"%s\", \"type\": \"quad\", \"id\": %s }", opQuad.getQuad(), identifiers.getIds(opQuad).get(0));
    }

    @Override
    public void visit(OpProject opProject) {
        opProject.getSubOp().visit(this);
    }

    @Override
    public void visit(OpSlice opSlice) {
        opSlice.getSubOp().visit(this);
    }

    /* @Override
    public void visit(OpFilter opFilter) {
        result += String.format("{ 'type' = 'filter', 'op' = '%s', 'children' = [ ");
    }*/

}
