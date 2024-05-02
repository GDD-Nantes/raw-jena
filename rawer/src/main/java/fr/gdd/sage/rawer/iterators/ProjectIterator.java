package fr.gdd.sage.rawer.iterators;

import fr.gdd.sage.sager.BindingId2Value;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;

import java.util.Iterator;
import java.util.Objects;

/**
 * Probably will be part of sage instead of this. Because there are no specificities.
 * It filters out the bindings based on targeted variables.
 */
public class ProjectIterator implements Iterator<BindingId2Value> {

    final Iterator<BindingId2Value> wrapped;
    final OpProject project;

    public ProjectIterator(OpProject project, Iterator<BindingId2Value> wrapped) {
        this.wrapped = wrapped;
        this.project = project;
    }

    @Override
    public boolean hasNext() {
        return wrapped.hasNext();
    }

    @Override
    public BindingId2Value next() {
        BindingId2Value b2v = new BindingId2Value();
        BindingId2Value current = wrapped.next();
        for (Var v : this.project.getVars()) {
            BindingId2Value.IdValueTable forV = current.getIdValueTable(v);
            if (Objects.nonNull(forV)) {
                b2v.put(v, forV.getId(), forV.getTable());
            }
        }
        return b2v;
    }
}
