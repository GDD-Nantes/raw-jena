package fr.gdd.sage.rawer.iterators;

import fr.gdd.sage.sager.BindingId2Value;
import fr.gdd.sage.sager.iterators.SagerScanFactory;
import org.apache.jena.atlas.lib.tuple.Tuple3;
import org.apache.jena.sparql.algebra.op.OpTriple;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.tdb2.store.NodeId;

import java.util.Iterator;

public class RandomScanFactory extends SagerScanFactory {

    public RandomScanFactory(Iterator<BindingId2Value> input, ExecutionContext context, OpTriple triple) {
        super(input, context, triple);
    }

    @Override
    public boolean hasNext() {
        if (!getInstantiated().hasNext() && !getInput().hasNext()) {
            return false;
        } else while (!getInstantiated().hasNext() && getInput().hasNext()) {
            setBinding(getInput().next());
            Tuple3<NodeId> spo = substitute(getTriple().getTriple(), getBinding());

            setInstantiated(new RandomScan(getContext(), getTriple(), spo,
                    getBackend().search(spo.get(0), spo.get(1), spo.get(2))));
        }

        return getInstantiated().hasNext();
    }

    @Override
    public BindingId2Value next() {
        return getInstantiated().next().setParent(getBinding());
    }

}
