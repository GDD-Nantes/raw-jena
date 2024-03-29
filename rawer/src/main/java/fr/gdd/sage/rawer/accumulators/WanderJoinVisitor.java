package fr.gdd.sage.rawer.accumulators;

import fr.gdd.jena.visitors.ReturningOpVisitor;
import fr.gdd.sage.rawer.iterators.RandomScan;
import fr.gdd.sage.sager.BindingId2Value;
import fr.gdd.sage.sager.HashMapWithPtrs;
import fr.gdd.sage.sager.pause.Save2SPARQL;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpTriple;

import java.util.Iterator;

/**
 * Process the probability of having retrieved the last random walk.
 */
public class WanderJoinVisitor extends ReturningOpVisitor<Double> {

    public final HashMapWithPtrs<Op, Iterator<BindingId2Value>> op2it;

    public WanderJoinVisitor (HashMapWithPtrs<Op, Iterator<BindingId2Value>> op2it) {
        this.op2it = op2it;
    }

    @Override
    public Double visit(OpTriple triple) {
        RandomScan scan = (RandomScan) op2it.get(triple);
        return scan.getProbability();
    }
}
