package fr.gdd.sage.rawer;

import fr.gdd.jena.visitors.ReturningArgsOpVisitor;
import fr.gdd.jena.visitors.ReturningArgsOpVisitorRouter;
import fr.gdd.jena.visitors.ReturningOpVisitorRouter;
import fr.gdd.sage.jena.JenaBackend;
import fr.gdd.sage.rawer.accumulators.ApproximateAggCount;
import fr.gdd.sage.rawer.iterators.ProjectIterator;
import fr.gdd.sage.rawer.iterators.RandomRoot;
import fr.gdd.sage.rawer.iterators.RandomScanFactory;
import fr.gdd.sage.sager.BindingId2Value;
import fr.gdd.sage.sager.SagerConstants;
import fr.gdd.sage.sager.pause.Save2SPARQL;
import fr.gdd.sage.sager.resume.BGP2Triples;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterAssign;
import org.apache.jena.sparql.engine.iterator.QueryIterGroup;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.aggregate.AggCount;

import java.util.Iterator;

/**
 * Execute the query and exactly the query that has been asked.
 * If an operator is not implemented, then it returns the explicit mention
 * that it's not implemented. No surprises.
 */
public class RawerOpExecutor extends ReturningArgsOpVisitor<Iterator<BindingId2Value>, Iterator<BindingId2Value>> {

    final ExecutionContext execCxt;
    final JenaBackend backend;

    public RawerOpExecutor(ExecutionContext execCxt) {
        this.execCxt = execCxt;
        execCxt.getContext().setIfUndef(RawerConstants.BACKEND, new JenaBackend(execCxt.getDataset()));
        backend = execCxt.getContext().get(RawerConstants.BACKEND);
        execCxt.getContext().setIfUndef(SagerConstants.BACKEND, backend);
        execCxt.getContext().setIfUndef(RawerConstants.SCANS, 0L);
    }

    public RawerOpExecutor setTimeout(Long timeout) {
        execCxt.getContext().set(RawerConstants.DEADLINE, System.currentTimeMillis()+timeout);
        return this;
    }

    public RawerOpExecutor setLimit(Long limit) {
        execCxt.getContext().set(RawerConstants.LIMIT, limit);
        return this;
    }

    /* ************************************************************************ */

    public QueryIterator execute(Op root) {
        root = ReturningOpVisitorRouter.visit(new BGP2Triples(), root); // TODO fix
        execCxt.getContext().set(SagerConstants.SAVER, new Save2SPARQL(root, execCxt));
        // Iterator<BindingId2Value> wrapped = new RandomRoot(this, execCxt, root);
        Iterator<BindingId2Value> wrapped = ReturningArgsOpVisitorRouter.visit(this, root, Iter.of(new BindingId2Value()));
        return biv2qi(wrapped, execCxt);
    }

    @Override
    public Iterator<BindingId2Value> visit(OpTriple triple, Iterator<BindingId2Value> input) {
        return new RandomScanFactory(input, execCxt, triple);
    }

    @Override
    public Iterator<BindingId2Value> visit(OpProject project, Iterator<BindingId2Value> input) {
        return new ProjectIterator(project, ReturningArgsOpVisitorRouter.visit(this, project.getSubOp(), input));
    }

    @Override
    public Iterator<BindingId2Value> visit(OpExtend extend, Iterator<BindingId2Value> input) {
        Iterator<BindingId2Value> wrapped = ReturningArgsOpVisitorRouter.visit(this, extend.getSubOp(), input);
        // TODO implement a QueryIterAssign that returns BindingId2Value directly
        QueryIterator qi = new QueryIterAssign(biv2qi(wrapped, execCxt), extend.getVarExprList(), execCxt, true);
        return qi2biv(qi, backend);
    }

    @Override
    public Iterator<BindingId2Value> visit(OpJoin join, Iterator<BindingId2Value> input) {
        input = ReturningArgsOpVisitorRouter.visit(this, join.getLeft(), input);
        return ReturningArgsOpVisitorRouter.visit(this, join.getRight(), input);
    }

    @Override
    public Iterator<BindingId2Value> visit(OpTable table, Iterator<BindingId2Value> input) {
        if (table.isJoinIdentity())
            return input;
        throw new UnsupportedOperationException("TODO: VALUES…"); // TODO
    }

    @Override
    public Iterator<BindingId2Value> visit(OpGroup groupBy, Iterator<BindingId2Value> input) {
        // QueryIterator qIter = exec(opGroup.getSubOp(), input);
        // qIter = new QueryIterGroup(qIter, opGroup.getGroupVars(), opGroup.getAggregators(), execCxt);
        // return qIter;
        if (groupBy.getAggregators().stream().anyMatch(a -> !(a.getAggregator() instanceof AggCount))) {
            throw new UnsupportedOperationException("An aggregation function is not implemented.");
        }
        for (int i = 0; i < groupBy.getAggregators().size(); ++i){
            if (groupBy.getAggregators().get(i).getAggregator() instanceof AggCount) {
                groupBy.getAggregators().set(i, new ExprAggregator(groupBy.getAggregators().get(i).getVar(), new ApproximateAggCount(execCxt, groupBy.getSubOp())));
            }
        }

        //vv wrapped = ReturningArgsOpVisitorRouter.visit(this, groupBy.getSubOp(), input);
        Iterator<BindingId2Value> wrapped = new RandomRoot(this, execCxt, groupBy.getSubOp());
        return  qi2biv(new QueryIterGroup(biv2qi(wrapped, execCxt),
                groupBy.getGroupVars(), groupBy.getAggregators(), execCxt), backend);
    }

    /* ****************************************************************** */

    /**
     * @param biv The iterator used in this query executor.
     * @param ec The execution context of the query.
     * @return The QueryIterator that is most used in Apache Jena.
     */
    public static QueryIterator biv2qi (Iterator<BindingId2Value> biv, ExecutionContext ec) {
        return QueryIterPlainWrapper.create(Iter.map(biv, bnid -> bnid), ec);
    }

    /**
     * @param qi The QueryIterator that is most used in Apache Jena.
     * @param backend The backend to provide a default table to use to retrieve Node and NodeId.
     * @return An iterator suited for this engine.
     */
    public static Iterator<BindingId2Value> qi2biv(QueryIterator qi, JenaBackend backend) {
        return new Iterator<BindingId2Value>() {
            @Override
            public boolean hasNext() {
                return qi.hasNext();
            }

            @Override
            public BindingId2Value next() {
                Binding binding = qi.next();
                return switch (binding) {
                    case BindingId2Value biv -> biv;
                    default -> {
                        // TODO change default table
                        BindingId2Value biv = new BindingId2Value().setDefaultTable(backend.getNodeTripleTable());
                        for (Iterator<Var> it = binding.vars(); it.hasNext(); ) {
                            Var v = it.next();
                            biv.put(v, binding.get(v));
                        }
                        yield biv;
                    }
                };
            }
        };
    }



}
