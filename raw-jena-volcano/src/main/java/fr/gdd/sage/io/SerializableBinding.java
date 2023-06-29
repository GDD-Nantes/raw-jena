package fr.gdd.sage.io;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

public class SerializableBinding extends HashMap<String, String> implements Serializable {

    // (TODO) serialize "type" then "value", eg.
    /*{
        "s": { "type": "uri" , "value": "http://db.uwaterloo.ca/~galuc/wsdbm/User75897" } ,
        "p": { "type": "uri" , "value": "http://db.uwaterloo.ca/~galuc/wsdbm/follows" } ,
        "o": { "type": "uri" , "value": "http://db.uwaterloo.ca/~galuc/wsdbm/User4315" }
    }*/

    public SerializableBinding (Binding binding) {
        super();
        Iterator<Var> vars = binding.vars();
        while (vars.hasNext()) {
            Var var = vars.next();
            this.put(var.toString(), binding.get(var).toString());
        }
    }
}
