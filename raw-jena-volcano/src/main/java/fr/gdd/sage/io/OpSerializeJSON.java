package fr.gdd.sage.io;

import fr.gdd.sage.arq.IdentifierAllocator;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.*;

import java.util.List;
import java.util.stream.Collectors;

public class OpSerializeJSON extends OpVisitorUnimplemented {

    String result = "";
    IdentifierAllocator identifiers;

    public OpSerializeJSON(Op op) {
        this.identifiers = new IdentifierAllocator();
        op.visit(this.identifiers);
        op.visit(this);
     //   result = "[" + result + "]";
    }

    @Override
    public void visit(OpBGP opBGP) {
        List<JsonObject> bgp = Streams.zip(opBGP.getPattern().getList().stream(), this.identifiers.getIds(opBGP).stream(),
                        (t, i) -> {
                            JsonObject jo = new JsonObject();
                            jo.put("name", t.toString());
                            jo.put("type", "triple");
                            jo.put("id", i);
                            return jo;
                        }).collect(Collectors.toList());
        while (bgp.size() > 1) {
            JsonObject removed = bgp.remove(bgp.size() - 1);
            JsonArray children = new JsonArray();
            children.add(removed);
            bgp.get(bgp.size() - 1).put("children", children);
        }
        this.result = bgp.get(0).toString();
    }

    @Override
    public void visit(OpQuadPattern opQuadPattern) {
        List<JsonObject> quad = Streams.zip(opQuadPattern.getPattern().getList().stream(), this.identifiers.getIds(opQuadPattern).stream(),
                (q, i) -> {
                    JsonObject jo = new JsonObject();
                    jo.put("name", q.toString());
                    jo.put("type", "quad");
                    jo.put("id", i);
                    return jo;
                }).collect(Collectors.toList());
        while (quad.size() > 1) {
            JsonObject removed = quad.remove(quad.size() - 1);
            JsonArray children = new JsonArray();
            children.add(removed);
            quad.get(quad.size() - 1).put("children", children);
        }
        this.result = quad.get(0).toString();
    }

    /* @Override
    public void visit(OpTriple opTriple) {
        result += String.format("{ \"name\": \"%s\", \"type\": \"triple\", \"id\": %s }", opTriple.getTriple(), identifiers.getIds(opTriple).get(0));
    }


    @Override
    public void visit(OpQuad opQuad) {
        result += String.format("{ \"name\": \"%s\", \"type\": \"quad\", \"id\": %s }", opQuad.getQuad(), identifiers.getIds(opQuad).get(0));
    }
    
     */

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
