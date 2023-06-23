package fr.gdd.fedup;

import fr.gdd.sage.optimizer.GraphClauseAdder;
import fr.gdd.sage.optimizer.SageOptimizer;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;
import org.apache.jena.query.Dataset;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.OpVisitorByType;
import org.apache.jena.sparql.algebra.Transformer;

import java.util.ArrayList;
import java.util.List;

/**
 * Mostly a utility function to create sequences of genes. A `Plan` is a
 * tree of `Op`. Every `Op` is referenced by an `OpGene`.
 */
public class PlanChromosome implements Chromosome<OpGene> {

    List<OpGene> genes = new ArrayList<>();

    public PlanChromosome() {}

    public PlanChromosome(PlanChromosome other) {
        this.genes.addAll(other.genes);
    }

    public PlanChromosome(Op op, Dataset dataset) {
        // we add graph clauses
        Op graphed = Transformer.transform(new GraphClauseAdder(), op);
        // starts from basic order provided by Sage
        Op transformed = Transformer.transform(new SageOptimizer(dataset), graphed);
        // populate genes
        transformed.visit(new OpGeneWrapper(this.genes));
    }

    @Override
    public Chromosome<OpGene> newInstance(ISeq<OpGene> iSeq) {
        PlanChromosome pc = new PlanChromosome();
        for (OpGene gene : iSeq) {
            pc.genes.add(gene);
        }
        return pc;
    }

    @Override
    public OpGene get(int i) {
        return genes.get(i);
    }

    @Override
    public int length() {
        return genes.size();
    }

    @Override
    public Chromosome<OpGene> newInstance() {
        return new PlanChromosome(this);
    }

    @Override
    public String toString() {
        return this.genes.get(0).toString();
    }
}
