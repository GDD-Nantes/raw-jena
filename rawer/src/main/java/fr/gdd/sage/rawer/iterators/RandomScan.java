package fr.gdd.sage.rawer.iterators;

import fr.gdd.sage.interfaces.BackendIterator;
import fr.gdd.sage.interfaces.SPOC;
import fr.gdd.sage.sager.BindingId2Value;
import fr.gdd.sage.sager.iterators.SagerScan;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.tdb2.store.NodeId;

import java.util.Objects;

/**
 * A scan executes only once, in random settings.
 */
public class RandomScan extends SagerScan {

    public boolean consumed = false;

    public RandomScan(ExecutionContext context, OpTriple op, Tuple<NodeId> spo, BackendIterator<NodeId, ?> wrapped) {
        super(context, op, spo, wrapped);
    }

    @Override
    public boolean hasNext() {
        return !consumed && super.hasNext();
    }

    @Override
    public BindingId2Value next() {
        // TODO quads
        Tuple<NodeId> randomSPO = getProgressJenaIterator().getRandomSPO();
        BindingId2Value current = new BindingId2Value().setDefaultTable(backend.getNodeTripleTable());

        if (Objects.nonNull(vars.get(0))) { // ugly x3
            current.put(vars.get(0), randomSPO.get(SPOC.SUBJECT));
        }
        if (Objects.nonNull(vars.get(1))) {
            current.put(vars.get(1), randomSPO.get(SPOC.PREDICATE));
        }
        if (Objects.nonNull(vars.get(2))) {
            current.put(vars.get(2), randomSPO.get(SPOC.OBJECT));
        }

        return current;
    }
}
