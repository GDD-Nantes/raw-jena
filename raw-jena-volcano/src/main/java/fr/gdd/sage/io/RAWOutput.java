package fr.gdd.sage.io;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.riot.rowset.RowSetWriter;
import org.apache.jena.riot.rowset.rw.RowSetWriterJSON;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.RAWJenaIteratorWrapper;
import org.apache.jena.sparql.exec.RowSet;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * Objects being returned to the client. It contains the most of random walks: individual bindings
 * with their respective cardinality. Of course, it is also the most expensive, especially on
 * traffic since it may be sent through the network.
 * <br />
 * Depending on the application, it may be wiser to get an aggregated version of these output.
 */
public class RAWOutput implements Serializable {

    Integer nbScans = 0;

    List<HashMap<Integer, Long>> cardinalities = new ArrayList<>();
    List<Binding> bindings = new ArrayList<>();
    Op plan;

    List<String> vars = new ArrayList<>();

    public RAWOutput(Op plan) {
        this.plan = plan;
    }

    /**
     * Another scan has been performed on a {@link org.apache.jena.dboe.trans.bplustree.BPlusTree}.
     */
    public void addScan() {
        nbScans += 1;
    }

    public Integer getNbScans() {
        return nbScans;
    }

    /**
     * The root registers a new random result.
     * @param iterators The map id_of_iterator to actual iterator.
     */
    public void addResultThenClear(HashMap<Integer, RAWJenaIteratorWrapper> iterators) {
        BindingBuilder b = BindingBuilder.create();
        HashMap<Integer, Long> c = new HashMap<>();
        for (Map.Entry<Integer, RAWJenaIteratorWrapper> kv : iterators.entrySet()) {
            b.addAll(kv.getValue().getCurrent());
            c.put(kv.getKey(), kv.getValue().getCardinality());
        }
        iterators.clear();
        cardinalities.add(c);
        Binding built = b.build();
        bindings.add(built);

        Iterator<Var> varsOfBinding = built.vars();
        while (varsOfBinding.hasNext()) {
            Var v = varsOfBinding.next();
            if (!this.vars.contains(v.getVarName())) {
                this.vars.add(v.getVarName());
            }
        }
    }

    public String getBindings() {
        ResultSet rs = ResultSetFactory.create(new QueryIteratorFromBindings(bindings), vars);
        RowSet rowSet = RowSet.adapt(rs);
        RowSetWriter rswj = RowSetWriterJSON.factory.create(ResultSetLang.RS_JSON);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rswj.write(baos, rowSet, null);
        return baos.toString();
    }

    public List<HashMap<Integer, Long>> getCardinalities() {
        return cardinalities;
    }

    public String getPlan() {
        return (new OpSerializeJSON(this.plan)).result;
    }
}
