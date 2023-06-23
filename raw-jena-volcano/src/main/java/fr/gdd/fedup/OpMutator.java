package fr.gdd.fedup;

import io.jenetics.Chromosome;
import io.jenetics.Mutator;
import io.jenetics.MutatorResult;
import io.jenetics.internal.math.Randoms;
import org.apache.jena.base.Sys;
import org.apache.jena.sparql.algebra.*;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;

import java.util.random.RandomGenerator;

/**
 * Choose `Op` of the `Plan` at random and mutate them depending on their type.
 */
public class OpMutator extends Mutator<OpGene, Integer> {

    @Override
    protected MutatorResult<Chromosome<OpGene>> mutate(Chromosome<OpGene> chromosome, double p, RandomGenerator random) {
        int mutations = (int) Randoms.indexes(random, chromosome.length(), p).peek(i -> {
            chromosome.get(i).operator = Transformer.transform(new OpMutatorVisitor(), chromosome.get(i).operator);
        }).count();

        return new MutatorResult<>(new PlanChromosome((PlanChromosome) chromosome), mutations);
    }

    /**
     * Default transformations allowed.
     */
    public static class OpMutatorVisitor extends TransformCopy {

        @Override
        public Op transform(OpLeftJoin opLeftJoin, Op left, Op right) {
            return OpJoin.create(left, right); // OPT removed
        }

        @Override
        public Op transform(OpJoin opJoin, Op left, Op right) {
            return super.transform(opJoin, right, left); // invert sides
        }
    }

}
