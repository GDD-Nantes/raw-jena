package fr.gdd.sage.rawer.iterators;

import fr.gdd.sage.generics.BackendBindings;
import fr.gdd.sage.interfaces.Backend;
import fr.gdd.sage.rawer.RawerConstants;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple3;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;

import java.util.Iterator;
import java.util.Objects;

public class RandomScanFactory<ID, VALUE> implements Iterator<BackendBindings<ID, VALUE>> {

    final Backend<ID, VALUE, ?> backend;
    final Iterator<BackendBindings<ID, VALUE>> input;
    final ExecutionContext context;
    final OpTriple triple;

    BackendBindings<ID, VALUE> inputBinding;
    Iterator<BackendBindings<ID, VALUE>> instantiated = Iter.empty();

    public RandomScanFactory(Iterator<BackendBindings<ID, VALUE>> input, ExecutionContext context, OpTriple triple) {
        this.input = input;
        this.context = context;
        this.triple = triple;
        this.backend = context.getContext().get(RawerConstants.BACKEND);
    }

    @Override
    public boolean hasNext() {
        if (!instantiated.hasNext() && !input.hasNext()) {
            return false;
        } else while (!instantiated.hasNext() && input.hasNext()) {
            inputBinding = input.next();
            Tuple3<ID> spo = substitute(triple.getTriple(), inputBinding);

            instantiated = new RandomScan<>(context, triple, spo);
        }

        return instantiated.hasNext();
    }

    @Override
    public BackendBindings<ID, VALUE> next() {
        return instantiated.next().setParent(inputBinding);
    }

    /* ***************************************************************** */

    protected Tuple3<ID> substitute(Triple triple, BackendBindings<ID, VALUE> binding) {
        return TupleFactory.create3(substitute(triple.getSubject(), binding),
                substitute(triple.getPredicate(),binding),
                substitute(triple.getObject(), binding));
    }

    protected ID substitute(Node sOrPOrO, BackendBindings<ID, VALUE> binding) {
        if (sOrPOrO.isVariable()) {
            BackendBindings.IdValueBackend<ID, VALUE> b = binding.get(Var.alloc(sOrPOrO));
            return Objects.isNull(b) ? null : b.getId();
        } else {
            return backend.getId(sOrPOrO.toString());
        }
    }
}
