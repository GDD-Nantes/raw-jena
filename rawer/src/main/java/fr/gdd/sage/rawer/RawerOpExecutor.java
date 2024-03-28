package fr.gdd.sage.rawer;

import fr.gdd.jena.visitors.ReturningArgsOpVisitor;
import fr.gdd.jena.visitors.ReturningArgsOpVisitorRouter;
import fr.gdd.jena.visitors.ReturningOpVisitorRouter;
import fr.gdd.sage.jena.JenaBackend;
import fr.gdd.sage.rawer.iterators.ProjectIterator;
import fr.gdd.sage.rawer.iterators.RandomRoot;
import fr.gdd.sage.rawer.iterators.RandomScanFactory;
import fr.gdd.sage.sager.BindingId2Value;
import fr.gdd.sage.sager.SagerConstants;
import fr.gdd.sage.sager.pause.Save2SPARQL;
import fr.gdd.sage.sager.resume.BGP2Triples;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

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

        Iterator<BindingId2Value> wrapped = new RandomRoot(this, execCxt, root);

        return QueryIterPlainWrapper.create(Iter.map(wrapped, bnid -> {
            BindingBuilder builder = BindingFactory.builder();
            for (Var var : bnid) {
                builder.add(var, bnid.getValue(var));
            }
            return builder.build();
        }), execCxt);
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
    public Iterator<BindingId2Value> visit(OpExtend extend, Iterator<BindingId2Value> args) {
        return super.visit(extend, args);
    }
}
