package fr.gdd.sage.rawer.iterators;

import fr.gdd.sage.generics.BackendBindings;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;

import java.util.Iterator;
import java.util.Objects;

/**
 * Probably will be part of sage instead of this. Because there are no specificities.
 * It filters out the bindings based on targeted variables.
 */
public class ProjectIterator<ID, VALUE> implements Iterator<BackendBindings<ID, VALUE>> {

    final Iterator<BackendBindings<ID, VALUE>> wrapped;
    final OpProject project;

    public ProjectIterator(OpProject project, Iterator<BackendBindings<ID, VALUE>> wrapped) {
        this.wrapped = wrapped;
        this.project = project;
    }

    @Override
    public boolean hasNext() {
        return wrapped.hasNext();
    }

    @Override
    public BackendBindings<ID, VALUE> next() {
        BackendBindings<ID, VALUE> b2v = new BackendBindings<>();
        BackendBindings<ID, VALUE> current = wrapped.next();
        for (Var v : this.project.getVars()) {
            BackendBindings.IdValueBackend<ID, VALUE> forV = current.get(v);
            if (Objects.nonNull(forV)) {
                b2v.put(v, forV);
            }
        }
        return b2v;
    }
}
