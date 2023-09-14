package fr.gdd.raw.identifiers;

import fr.gdd.sage.generics.Pair;
import org.apache.jena.base.Sys;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.tdb.store.Hash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Associate with each operator an identifier, or a range of identifiers,
 * that it can allocate. RAW's identifiers are useful to collect metadata
 * and map them to the original query plan.
 */
public class IdentifierAllocator extends OpVisitorBase {

    HashMap<Op, List<Pair<Op, List<Integer>>>> op2Id = new HashMap<>();
    HashMap<Integer, Op> id2Op = new HashMap<>();

    Integer current = 1;

    public IdentifierAllocator() {}

    public IdentifierAllocator(Integer start) {
        current = start;
    }

    public Integer getCurrent() {
        return current;
    }

    public List<Integer> getIds (Op op) {
        List<Pair<Op, List<Integer>>> opList = this.op2Id.get(op);
        if (opList.size() == 1) {
            // if there is but one element, we do not compare pointer.
            // In most cases its sufficient.
            // Among others : easier for testing purposes.
            return opList.get(0).getRight();
        }

        for (Pair<Op, List<Integer>> opAndIds : opList ) {
            if (opAndIds.getLeft() == op) {
                return opAndIds.getRight();
            }
        }
        throw new RuntimeException("Could not find " + op + " in the map of Ops");
    }

    private void addOp2Ids(Op op, List<Integer> ids) {
        if (!op2Id.containsKey(op)) {
            op2Id.put(op, new ArrayList<>());
        }
        op2Id.get(op).add(new Pair<>(op, ids));
    }

    /* ******************************************************************** */

    @Override
    public void visit(OpTriple opTriple) {
        addOp2Ids(opTriple, List.of(current));
        id2Op.put(current, opTriple);
        current += 1;
    }

    @Override
    public void visit(OpQuad opQuad) {
        addOp2Ids(opQuad, List.of(current));
        id2Op.put(current, opQuad);
        current += 1;
    }

    @Override
    public void visit(OpBGP opBGP) {
        List<Integer> ids = new ArrayList<>();
        for (int i= 0; i < opBGP.getPattern().size(); ++i) {
            ids.add(current + i);
            id2Op.put(current + i, opBGP);
        }
        addOp2Ids(opBGP, ids);
        current += opBGP.getPattern().size();
    }

    @Override
    public void visit(OpQuadPattern quadPattern) {
        List<Integer> ids = new ArrayList<>();
        for (int i= 0; i < quadPattern.getPattern().size(); ++i) {
            ids.add(current + i);
            id2Op.put(current + i, quadPattern);
        }
        addOp2Ids(quadPattern, ids);
        current += quadPattern.getPattern().size();
    }

    @Override
    public void visit(OpFilter opFilter) {
        opFilter.getSubOp().visit(this);
    }

    @Override
    public void visit(OpProject opProject) {
        opProject.getSubOp().visit(this);
    }

    @Override
    public void visit(OpLeftJoin opLeftJoin) {
        opLeftJoin.getLeft().visit(this);
        // one id dedicated to optional
        addOp2Ids(opLeftJoin, List.of(current));
        id2Op.put(current, opLeftJoin);
        current += 1;
        opLeftJoin.getRight().visit(this);
    }

    @Override
    public void visit(OpConditional opConditional) {
        opConditional.getLeft().visit(this);
        // one id dedicated to optional
        addOp2Ids(opConditional, List.of(current));
        id2Op.put(current, opConditional);
        current += 1;
        opConditional.getRight().visit(this);
    }

    @Override
    public void visit(OpJoin opJoin) {
        opJoin.getLeft().visit(this);
        opJoin.getRight().visit(this);
    }

    @Override
    public void visit(OpUnion opUnion) {
        List<Op> flattened = flattenUnion(opUnion);
        addOp2Ids(opUnion, List.of(current));  // one id dedicated to multi-union
        id2Op.put(current, opUnion);
        current += 1;
        for (Op subOp : flattened) {
            subOp.visit(this);
        }
    }

    @Override
    public void visit(OpSlice opSlice) {
        opSlice.getSubOp().visit(this);
        addOp2Ids(opSlice, List.of(current));
        id2Op.put(current, opSlice);
        current += 1;
    }

    /* ************************************************************************************ */

    // Based on code from Olaf Hartig.
    // Comes from {@link OpExecutorTDB}
    public static List<Op> flattenUnion(OpUnion opUnion) {
        List<Op> x = new ArrayList<>();
        flattenUnion(x, opUnion);
        return x;
    }

    public static void flattenUnion(List<Op> acc, OpUnion opUnion) {
        if ( opUnion.getLeft() instanceof OpUnion )
            flattenUnion(acc, (OpUnion)opUnion.getLeft());
        else
            acc.add(opUnion.getLeft());

        if ( opUnion.getRight() instanceof OpUnion )
            flattenUnion(acc, (OpUnion)opUnion.getRight());
        else
            acc.add(opUnion.getRight());
    }

}
