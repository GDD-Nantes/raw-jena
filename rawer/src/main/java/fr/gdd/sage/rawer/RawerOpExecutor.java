package fr.gdd.sage.rawer;

import fr.gdd.jena.visitors.ReturningArgsOpVisitor;
import fr.gdd.jena.visitors.ReturningArgsOpVisitorRouter;
import fr.gdd.jena.visitors.ReturningOpVisitorRouter;
import fr.gdd.sage.generics.BackendBindings;
import fr.gdd.sage.interfaces.Backend;
import fr.gdd.sage.rawer.iterators.ProjectIterator;
import fr.gdd.sage.rawer.iterators.RandomRoot;
import fr.gdd.sage.rawer.iterators.RandomScanFactory;
import fr.gdd.sage.sager.SagerConstants;
import fr.gdd.sage.sager.pause.Save2SPARQL;
import fr.gdd.sage.sager.resume.BGP2Triples;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.engine.ExecutionContext;

import java.util.Iterator;

/**
 * Execute the query and exactly the query that has been asked.
 * If an operator is not implemented, then it returns the explicit mention
 * that it's not implemented. No surprises.
 */
public class RawerOpExecutor<ID, VALUE> extends ReturningArgsOpVisitor<
        Iterator<BackendBindings<ID, VALUE>>, // input
        Iterator<BackendBindings<ID, VALUE>>> { // output

    final ExecutionContext execCxt;
    final Backend<ID, VALUE, ?> backend;

    public RawerOpExecutor(ExecutionContext execCxt) {
        this.execCxt = execCxt;
        backend = execCxt.getContext().get(RawerConstants.BACKEND);
        execCxt.getContext().setIfUndef(RawerConstants.SCANS, 0L);
        execCxt.getContext().setIfUndef(RawerConstants.LIMIT, Long.MAX_VALUE);
        execCxt.getContext().setIfUndef(RawerConstants.TIMEOUT, Long.MAX_VALUE);
    }

    public RawerOpExecutor<ID, VALUE> setTimeout(Long timeout) {
        execCxt.getContext().set(RawerConstants.DEADLINE, System.currentTimeMillis()+timeout);
        return this;
    }

    public RawerOpExecutor<ID, VALUE> setLimit(Long limit) {
        execCxt.getContext().set(RawerConstants.LIMIT, limit);
        return this;
    }

    /* ************************************************************************ */

    public Iterator<BackendBindings<ID, VALUE>> execute(Op root) {
        root = ReturningOpVisitorRouter.visit(new BGP2Triples(), root); // TODO fix
        execCxt.getContext().set(SagerConstants.SAVER, new Save2SPARQL(root, execCxt));
        Iterator<BackendBindings<ID, VALUE>> wrapped = new RandomRoot<>(this, execCxt, root);
        // Iterator<BindingId2Value> wrapped = ReturningArgsOpVisitorRouter.visit(this, root, Iter.of(new BindingId2Value()));
        // return biv2qi(wrapped, execCxt);
        return wrapped;
    }

    @Override
    public Iterator<BackendBindings<ID, VALUE>> visit(OpTriple triple, Iterator<BackendBindings<ID, VALUE>> input) {
        return new RandomScanFactory<>(input, execCxt, triple);
    }

    @Override
    public Iterator<BackendBindings<ID, VALUE>> visit(OpProject project, Iterator<BackendBindings<ID, VALUE>> input) {
        return new ProjectIterator(project, ReturningArgsOpVisitorRouter.visit(this, project.getSubOp(), input));
    }


//    @Override
//    public Iterator<BackendBindings<ID, VALUE>> visit(OpExtend extend, Iterator<BackendBindings<ID, VALUE>> input) {
//        Iterator<BindingId2Value> wrapped = ReturningArgsOpVisitorRouter.visit(this, extend.getSubOp(), input);
//        // TODO implement a QueryIterAssign that returns BindingId2Value directly
//        QueryIterator qi = new QueryIterAssign(biv2qi(wrapped, execCxt), extend.getVarExprList(), execCxt, true);
//        return qi2biv(qi, backend);
//    }
//
//    @Override
//    public Iterator<BackendBindings<ID, VALUE>> visit(OpJoin join, Iterator<BackendBindings<ID, VALUE>> input) {
//        input = ReturningArgsOpVisitorRouter.visit(this, join.getLeft(), input);
//        return ReturningArgsOpVisitorRouter.visit(this, join.getRight(), input);
//    }
//
//    @Override
//    public Iterator<BackendBindings<ID, VALUE>> visit(OpTable table, Iterator<BackendBindings<ID, VALUE>> input) {
//        if (table.isJoinIdentity())
//            return input;
//        throw new UnsupportedOperationException("TODO: VALUES…"); // TODO
//    }
//
//    @Override
//    public Iterator<BackendBindings<ID, VALUE>> visit(OpGroup groupBy, Iterator<BackendBindings<ID, VALUE>> input) {
//        Long limit = execCxt.getContext().getLong(RawerConstants.LIMIT, 0L);
//        if (limit <= 0L) {
//            return input;
//        }
//
//        // execCxt.getContext().set(RawerConstants.LIMIT, (long) limit/2);
//        for (int i = 0; i < groupBy.getAggregators().size(); ++i) {
//            switch (groupBy.getAggregators().get(i).getAggregator()) {
//                case AggCount ac -> groupBy.getAggregators().set(i,
//                        new ExprAggregator(groupBy.getAggregators().get(i).getVar(),
//                            new ApproximateAggCount(execCxt, groupBy.getSubOp())));
//                case AggCountDistinct acd -> groupBy.getAggregators().set(i,
//                        new ExprAggregator(groupBy.getAggregators().get(i).getVar(),
//                            new ApproximateAggCountDistinct(execCxt, groupBy.getSubOp())));
//                default -> throw new UnsupportedOperationException("The aggregation function is not implemented: " +
//                        groupBy.getAggregators().get(i).toString());
//            }
//        }
//
//        //vv wrapped = ReturningArgsOpVisitorRouter.visit(this, groupBy.getSubOp(), input);
//        Iterator<BindingId2Value> wrapped = new RandomRoot(this, execCxt, groupBy.getSubOp());
//        return  qi2biv(new QueryIterGroup(biv2qi(wrapped, execCxt),
//                groupBy.getGroupVars(), groupBy.getAggregators(), execCxt), backend);
//    }
//
//    /* ****************************************************************** */
//
//    /**
//     * @param biv The iterator used in this query executor.
//     * @param ec The execution context of the query.
//     * @return The QueryIterator that is most used in Apache Jena.
//     */
//    public QueryIterator biv2qi(Iterator<BackendBindings<ID, VALUE>> biv, ExecutionContext ec) {
//        return QueryIterPlainWrapper.create(Iter.map(biv, bnid -> bnid), ec);
//    }
//
//    /**
//     * @param qi The QueryIterator that is most used in Apache Jena.
//     * @param backend The backend to provide a default table to use to retrieve Node and NodeId.
//     * @return An iterator suited for this engine.
//     */
//    public Iterator<BackendBindings<ID, VALUE>> qi2biv(QueryIterator qi, JenaBackend backend) {
//        return new Iterator<BackendBindings<ID, VALUE>>() {
//            @Override
//            public boolean hasNext() {
//                return qi.hasNext();
//            }
//
//            @Override
//            public BackendBindings<ID, VALUE> next() {
//                Binding binding = qi.next();
//                return switch (binding) {
//                    case BackendBindings<ID, VALUE> biv -> biv;
//                    default -> {
//                        // TODO change default table
//                        BindingId2Value biv = new BindingId2Value().setDefaultTable(backend.getNodeTripleTable());
//                        for (Iterator<Var> it = binding.vars(); it.hasNext(); ) {
//                            Var v = it.next();
//                            biv.put(v, binding.get(v));
//                        }
//                        yield biv;
//                    }
//                };
//            }
//        };
//    }



}
