package fr.gdd.fedup;

import io.jenetics.Gene;
import org.apache.jena.sparql.algebra.Op;

/**
 * The unit of plans. `OpGene` can mutate slightly depending on the kind of
 * `Op` they are. (TODO) They also could crossover, but the implementation is far more complicated.
 */
public class OpGene implements Gene<Op, OpGene> {

    Op operator;

    public OpGene(Op operator) {
        this.operator = operator;
    }

    @Override
    public Op allele() {
        return operator;
    }

    @Override
    public OpGene newInstance() {
        // (TODO) mutate here
        return new OpGene(operator);
    }

    @Override
    public OpGene newInstance(Op o) {
        return new OpGene(o);
    }

    @Override
    public boolean isValid() {
        // check if the value makes sense
        return true;
    }

    @Override
    public String toString() {
        return this.operator.toString();
    }
}
