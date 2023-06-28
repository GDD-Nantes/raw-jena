package fr.gdd.sage.io;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

public class SerializableBinding extends HashMap<String, String> implements Serializable {

    public SerializableBinding (Binding binding) {
        super();
        Iterator<Var> vars = binding.vars();
        while (vars.hasNext()) {
            Var var = vars.next();
            this.put(var.toString(), binding.get(var).toString());
        }
    }
}
