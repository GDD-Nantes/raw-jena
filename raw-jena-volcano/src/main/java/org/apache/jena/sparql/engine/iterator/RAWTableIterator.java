package org.apache.jena.sparql.engine.iterator;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.serializer.SerializationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RAWTableIterator implements QueryIterator {

    List<Binding> table; // unrolled bindings of table
    boolean isFirstExecution = true;

    public RAWTableIterator(Table table, ExecutionContext context) {
        this.table = new ArrayList<>();
        table.iterator(context).forEachRemaining(e -> {
            this.table.add(e);
        });
    }

    @Override
    public Binding nextBinding() {
        return next();
    }


    @Override
    public boolean hasNext() {
        return isFirstExecution && !table.isEmpty();
    }

    @Override
    public Binding next() {
        isFirstExecution = false;
        Random rng = new Random();
        int rn = rng.nextInt(table.size());
        return table.get(rn);
    }

    @Override
    public void output(IndentedWriter indentedWriter) {}

    @Override
    public void close() {}

    @Override
    public void output(IndentedWriter indentedWriter, SerializationContext serializationContext) {}

    @Override
    public String toString(PrefixMapping prefixMapping) {
        return null;
    }

    @Override
    public void cancel() {}
}
