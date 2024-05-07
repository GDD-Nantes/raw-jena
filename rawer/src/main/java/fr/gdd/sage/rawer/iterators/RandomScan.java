package fr.gdd.sage.rawer.iterators;

import fr.gdd.sage.generics.BackendBindings;
import fr.gdd.sage.interfaces.Backend;
import fr.gdd.sage.interfaces.BackendIterator;
import fr.gdd.sage.interfaces.SPOC;
import fr.gdd.sage.rawer.RawerConstants;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.Tuple3;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;

import java.util.Iterator;
import java.util.Objects;

/**
 * A scan executes only once, in random settings.
 */
public class RandomScan<ID, VALUE> implements Iterator<BackendBindings<ID, VALUE>> {

    boolean consumed = false;
    Tuple<ID> currentIDs;
    Double currentProbability;

    final ExecutionContext context;
    final OpTriple triple;
    final BackendIterator<ID, VALUE, ?> iterator;
    final Backend<ID, VALUE, ?> backend;
    final Tuple3<Var> vars;

    public RandomScan(ExecutionContext context, OpTriple triple, Tuple<ID> spo) {
        this.backend = context.getContext().get(RawerConstants.BACKEND);
        this.triple = triple;
        this.context = context;
        this.iterator = backend.search(spo.get(0), spo.get(1), spo.get(2));
        this.vars = TupleFactory.create3(
                triple.getTriple().getSubject().isVariable() && Objects.isNull(spo.get(0)) ? Var.alloc(triple.getTriple().getSubject()) : null,
                triple.getTriple().getPredicate().isVariable() && Objects.isNull(spo.get(1)) ? Var.alloc(triple.getTriple().getPredicate()) : null,
                triple.getTriple().getObject().isVariable() && Objects.isNull(spo.get(2)) ? Var.alloc(triple.getTriple().getObject()) : null);
    }

    @Override
    public boolean hasNext() {
        return !consumed && iterator.hasNext(); // at least 1 element, only called once anyway
    }

    @Override
    public BackendBindings<ID, VALUE> next() {
        consumed = true;
        this.currentProbability = iterator.random(); // position at random index
        iterator.next(); // read the value
        this.currentIDs = TupleFactory.create3(
                iterator.getId(SPOC.SUBJECT),
                iterator.getId(SPOC.PREDICATE),
                iterator.getId(SPOC.OBJECT));

        BackendBindings<ID, VALUE> newBinding = new BackendBindings<>();

        if (Objects.nonNull(vars.get(0))) { // ugly x3
            newBinding.put(vars.get(0), currentIDs.get(SPOC.SUBJECT), backend);
        }
        if (Objects.nonNull(vars.get(1))) {
            newBinding.put(vars.get(1), currentIDs.get(SPOC.PREDICATE), backend);
        }
        if (Objects.nonNull(vars.get(2))) {
            newBinding.put(vars.get(2), currentIDs.get(SPOC.OBJECT), backend);
        }

        return newBinding;
    }

    public Double getProbability() {
        return currentProbability;
    }
}
