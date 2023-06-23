package fr.gdd.fedup;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorByType;
import org.apache.jena.sparql.algebra.op.*;

import java.util.ArrayList;
import java.util.List;

public class OpGeneWrapper extends OpVisitorByType {

    List<OpGene> linearized;

    OpGeneWrapper(List<OpGene> linearized) {
        this.linearized = linearized;
    }

    @Override
    protected void visitN(OpN opn) {
        this.linearized.add(new OpGene(opn));
        for (Op op : opn.getElements()) {
            op.visit(this);
        }
    }

    @Override
    protected void visit2(Op2 op) {
        this.linearized.add(new OpGene(op));
        op.getLeft().visit(this);
        op.getRight().visit(this);
    }

    @Override
    protected void visit1(Op1 op) {
        linearized.add(new OpGene(op));
        op.getSubOp().visit(this);
    }

    @Override
    protected void visit0(Op0 op) {
        linearized.add(new OpGene(op));
    }

    @Override
    protected void visitFilter(OpFilter op) {
        linearized.add(new OpGene(op));
        op.getSubOp().visit(this);
    }

    @Override
    protected void visitLeftJoin(OpLeftJoin op) {
        linearized.add(new OpGene(op));
        op.getLeft().visit(this);
        op.getRight().visit(this);
    }
}
