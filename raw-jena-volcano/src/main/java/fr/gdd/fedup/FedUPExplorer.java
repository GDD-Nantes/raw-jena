package fr.gdd.fedup;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.algebra.Op;

/**
 * A mechanism for FedUP (a federated query engine) to explore the space of solution
 * by scrambling a little join orders. The intuition is that, rare solutions might come out
 * faster from weird join orders.
 */
public class FedUPExplorer {

    Dataset dataset;

    public FedUPExplorer(Dataset dataset) {
        this.dataset = dataset;
    }

    public void build (Op op) {
        // #1 we start from a basic plan with a cardinality-based heuristic
        Genotype<OpGene> genotype = Genotype.of(new PlanChromosome(op, dataset));


        Engine<OpGene, Integer> engine = Engine
                .builder(FedUPExplorer::fitness, genotype)
                .populationSize(15)
                .alterers(new OpMutator())
                // .offspringFraction(0.5)
                // .survivorsSelector(new TruncationSelector<>())
                // .offspringFraction(new MonteCarloSelector<OpGene,())
                .build();

        Genotype<OpGene> result = engine.stream()
                .limit(100)
                .collect(EvolutionResult.toBestGenotype());

        System.out.println(result);
    }

    private static Integer fitness(Genotype<OpGene> gt) {
        Chromosome<OpGene> pc = gt.chromosome();

        OpGene op = gt.gene();

        // (TODO) fitness function base on plan
        return 0;
    }

}


