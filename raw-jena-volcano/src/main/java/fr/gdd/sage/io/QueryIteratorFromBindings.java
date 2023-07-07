package fr.gdd.sage.io;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.serializer.SerializationContext;

import java.util.List;

/**
 * Convert a list of bindings into a {@link QueryIterator}, that can then be transformed
 * into a {@link org.apache.jena.sparql.exec.RowSet}, that can then be serialized into JSON.
 */
public class QueryIteratorFromBindings implements QueryIterator {

    List<Binding> results;
    int i = 0;

    QueryIteratorFromBindings(List<Binding> results) {
        this.results = results;
    }

    @Override
    public Binding nextBinding() {
        ++i;
        return results.get(i - 1);
    }

    @Override
    public boolean hasNext() {
        return i < results.size();
    }

    @Override
    public Binding next() {
        return this.nextBinding();
    }


    @Override
    public void cancel() {

    }

    @Override
    public void close() {

    }

    @Override
    public void output(IndentedWriter out) {
    }


    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {

    }

    @Override
    public String toString(PrefixMapping pmap) {
        return null;
    }
}
