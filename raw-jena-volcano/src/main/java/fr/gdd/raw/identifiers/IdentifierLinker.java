package fr.gdd.raw.identifiers;

import fr.gdd.sage.arq.SageConstants;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.OpVisitorByType;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.engine.ExecutionContext;

import java.util.*;

public class IdentifierLinker extends OpVisitorBase {

    HashMap<Integer, Integer> childToParent = new HashMap<>();
    IdentifierAllocator identifiers;

    GetMostLeftOp getLeftest = new GetMostLeftOp();
    GetMostRightOp getRightest = new GetMostRightOp();

    public static void create(ExecutionContext ec, Op op, boolean... force) {
        if (Objects.isNull(force) || force.length == 0 || !force[0]) {
            ec.getContext().setIfUndef(SageConstants.identifiers, new IdentifierLinker(op));
        } else {
            ec.getContext().set(SageConstants.identifiers, new IdentifierLinker(op));
        }
    }

    public IdentifierLinker(Op op) {
        this.identifiers = new IdentifierAllocator();
        op.visit(this.identifiers);
        op.visit(this);
    }

    public IdentifierLinker(Op op, Integer start) {
        this.identifiers = new IdentifierAllocator(start);
        op.visit(this.identifiers);
        op.visit(this);
    }

    public List<Integer> getIds(Op op) {
        return this.identifiers.getIds(op);
    }

    /* ******************************************************************* */

    @Override
    public void visit(OpProject opProject) {
        opProject.getSubOp().visit(this);
    }

    @Override
    public void visit(OpSlice opSlice) {
        opSlice.getSubOp().visit(getLeftest);
        Op opLeftest = getLeftest.result;

        Integer idLeftest = identifiers.getIds(opLeftest).stream().min(Integer::compare).orElseThrow();
        Integer idSlice = identifiers.getIds(opSlice).get(0);

        add(idSlice, idLeftest);

        opSlice.getSubOp().visit(this);
    }

    @Override
    public void visit(OpLeftJoin opLeftJoin) {
        opLeftJoin.getLeft().visit(getRightest);
        Op rightestOfLeftOp = getRightest.result;

        opLeftJoin.getRight().visit(getLeftest);
        Op leftestOfRightOp = getLeftest.result;

        Integer idRightestOfLeft = identifiers.getIds(rightestOfLeftOp).stream().max(Integer::compare).orElseThrow();
        Integer idLeftestOfRight = identifiers.getIds(leftestOfRightOp).stream().min(Integer::compare).orElseThrow();
        Integer idOptional = identifiers.getIds(opLeftJoin).get(0);

        add(idRightestOfLeft, idOptional);
        add(idOptional, idLeftestOfRight);

        opLeftJoin.getLeft().visit(this);
        opLeftJoin.getRight().visit(this);
    }

    @Override
    public void visit(OpConditional opCond) {
        opCond.getLeft().visit(getRightest);
        Op rightestOfLeftOp = getRightest.result;

        opCond.getRight().visit(getLeftest);
        Op leftestOfRightOp = getLeftest.result;

        Integer idRightestOfLeft = identifiers.getIds(rightestOfLeftOp).stream().max(Integer::compare).orElseThrow();
        Integer idLeftestOfRight = identifiers.getIds(leftestOfRightOp).stream().min(Integer::compare).orElseThrow();
        Integer idOptional = identifiers.getIds(opCond).get(0);

        add(idRightestOfLeft, idOptional);
        add(idOptional, idLeftestOfRight);

        opCond.getLeft().visit(this);
        opCond.getRight().visit(this);
    }

    @Override
    public void visit(OpJoin opJoin) {
        GetMostLeftOp visitor = new GetMostLeftOp();
        opJoin.getLeft().visit(visitor);
        Op leftestOp = visitor.result;

        opJoin.getRight().visit(visitor);
        Op leftestOfRightOp = visitor.result;

        Integer idLeftest = identifiers.getIds(leftestOp).stream().max(Integer::compare).orElseThrow();
        Integer idLeftestOfRight = identifiers.getIds(leftestOfRightOp).stream().min(Integer::compare).orElseThrow();

        add(idLeftest, idLeftestOfRight);

        opJoin.getLeft().visit(this);
        opJoin.getRight().visit(this);
    }

    @Override
    public void visit(OpBGP opBGP) {
        List<Integer> bgpIds = identifiers.getIds(opBGP);
        for (int i = 0; i < bgpIds.size() - 1; ++i) {
            add(bgpIds.get(i), bgpIds.get(i+1));
        }
    }

    @Override
    public void visit(OpQuadPattern opQuad) {
        List<Integer> quadIds = identifiers.getIds(opQuad);
        for (int i = 0; i < quadIds.size() - 1; ++i) {
            add(quadIds.get(i), quadIds.get(i+1));
        }
    }

    /* ***************************************************************************** */

    /**
     * State that the iterator child is the child of parent.
     * @param parent The identifier of the parent iterator.
     * @param child The child iterator.
     */
    public void add(Integer parent, Integer child) {
        childToParent.put(child, parent);
    }

    /**
     * @param child The unique identifier of the iterator
     * @return A list of parents of the iterator.
     */
    public Set<Integer> getParents(Integer child) {
        boolean done = false;
        Set<Integer> parents = new TreeSet<>();
        while (!done) {
            Integer parent = childToParent.get(child);
            if (Objects.nonNull(parent)) {
                parents.add(parent);
                child = parent;
            } else {
                done = true;
            }
        }
        return parents;
    }

    public boolean inRightSideOf(Integer parent, Integer child) {
        Op parentOp = identifiers.id2Op.get(parent);

        if (!getParents(child).contains(parent)) {
            return false;
        }

        if (parentOp instanceof Op2) {
            Op2 parentOp2 = (Op2) parentOp;

            Op childOp = identifiers.id2Op.get(child);
            IsIncludedIn visitor = new IsIncludedIn(childOp);
            parentOp2.getRight().visit(visitor);
            return visitor.result;
        } else if (parentOp instanceof Op1) {
            Op1 parentOp1 = (Op1) parentOp;

            Op childOp = identifiers.id2Op.get(child);
            IsIncludedIn visitor = new IsIncludedIn(childOp);
            parentOp1.getSubOp().visit(visitor);
            return visitor.result;
        }
        return false;
    }

    /* ******************************************************************* */

    static class IsIncludedIn extends OpVisitorByType {

        public boolean result = false;
        public Op toFind;

        public IsIncludedIn(Op toFind) {
            this.toFind = toFind;
        }

        @Override
        protected void visitN(OpN op) {
            result = result || op == toFind;
            int i = 0;
            while (!result && i < op.size()) {
                op.get(i).visit(this);
                ++i;
            }
        }

        @Override
        protected void visit2(Op2 op) {
            result = result || op == toFind;
            if (!result) op.getLeft().visit(this);
            if (!result) op.getRight().visit(this);
        }

        @Override
        protected void visit1(Op1 op) {
            result = result || op == toFind;
            if (!result) op.getSubOp().visit(this);
        }

        @Override
        protected void visit0(Op0 op) {
            result = result || op == toFind;
        }

        @Override
        protected void visitFilter(OpFilter op) {
            result = result || op == toFind;
            op.getSubOp().visit(this);
        }

        @Override
        protected void visitLeftJoin(OpLeftJoin op) {
            result = result || op == toFind;
            op.getLeft().visit(this);
            if (!result) op.getRight().visit(this);
        }
    }

    static class GetMostLeftOp extends OpVisitorByType {

        public Op result;

        @Override
        protected void visitN(OpN op) {
            op.get(0).visit(this);
        }

        @Override
        protected void visit2(Op2 op) {
            op.getLeft().visit(this);
        }

        @Override
        protected void visit1(Op1 op) {
            op.getSubOp().visit(this);
        }

        @Override
        protected void visit0(Op0 op) {
            result = op;
        }

        @Override
        protected void visitFilter(OpFilter op) {
            op.getSubOp().visit(this);
        }

        @Override
        protected void visitLeftJoin(OpLeftJoin op) {
            op.getLeft().visit(this);
        }
    }

    static class GetMostRightOp extends OpVisitorByType {

        public Op result;

        @Override
        protected void visitN(OpN op) {
            op.get(op.size()-1).visit(this);
        }

        @Override
        protected void visit2(Op2 op) {
            op.getRight().visit(this);
        }

        @Override
        protected void visit1(Op1 op) {
            op.getSubOp().visit(this);
        }

        @Override
        protected void visit0(Op0 op) {
            result = op;
        }

        @Override
        protected void visitFilter(OpFilter op) {
            op.getSubOp().visit(this);
        }

        @Override
        protected void visitLeftJoin(OpLeftJoin op) {
            op.getRight().visit(this);
        }
    }

}
