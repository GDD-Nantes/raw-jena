package fr.gdd.sage.rawer.iterators;

import fr.gdd.sage.interfaces.BackendIterator;
import fr.gdd.sage.interfaces.SPOC;
import fr.gdd.sage.rawer.RawerConstants;
import fr.gdd.sage.sager.BindingId2Value;
import fr.gdd.sage.sager.iterators.SagerScan;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.tdb2.store.NodeId;

import java.util.Objects;

/**
 * A scan executes only once, in random settings.
 */
public class RandomScan extends SagerScan {

    boolean consumed = false;
    Pair<Tuple<NodeId>, Double> current;
    ExecutionContext context;

    public RandomScan(ExecutionContext context, OpTriple op, Tuple<NodeId> spo, BackendIterator<NodeId, ?> wrapped) {
        super(context, op, spo, wrapped);
        this.context = context;
    }

    @Override
    public boolean hasNext() {
        return !consumed && super.hasNext();
    }

    @Override
    public BindingId2Value next() {
        // TODO quads
        consumed = true;
        context.getContext().set(RawerConstants.SCANS, context.getContext().getLong(RawerConstants.SCANS, 0L) + 1L);
        this.current = getProgressJenaIterator().getRandomSPOWithProbability();
        BindingId2Value currentBinding = new BindingId2Value().setDefaultTable(backend.getNodeTripleTable());

        if (Objects.nonNull(vars.get(0))) { // ugly x3
            currentBinding.put(vars.get(0), current.getLeft().get(SPOC.SUBJECT));
        }
        if (Objects.nonNull(vars.get(1))) {
            currentBinding.put(vars.get(1), current.getLeft().get(SPOC.PREDICATE));
        }
        if (Objects.nonNull(vars.get(2))) {
            currentBinding.put(vars.get(2), current.getLeft().get(SPOC.OBJECT));
        }

        return currentBinding;
    }

    public Double getProbability() {
        return current.getRight();
    }
}
