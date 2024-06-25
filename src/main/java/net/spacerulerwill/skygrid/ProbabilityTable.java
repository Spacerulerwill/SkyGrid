package net.spacerulerwill.skygrid;

import net.minecraft.util.math.random.Random;

public class ProbabilityTable<T> {
    public static final class Probability<T> {
        T object;
        double weight;

        Probability(T block, double weight) {
            this.object = block;
            this.weight = weight;
        }
    }

    private final Probability<T>[] probabilities;

    ProbabilityTable(Probability<T>[] probabilities) {
        this.probabilities = probabilities;

        // Calculate total weights
        double totalWeights = 0.0;
        for (Probability<T> probability : probabilities) {
            totalWeights += probability.weight;
        }

        // Cumulative weight normalization
        double accumulator = 0.0;
        for (Probability<T> probability : probabilities) {
            double normalized = probability.weight / totalWeights;
            accumulator += normalized;
            probability.weight = accumulator;
        }
    }

    public T pickRandom(Random random) {
        double rand = random.nextDouble();
        for (Probability<T> probability : probabilities) {
            if (rand <= probability.weight) {
                return probability.object;
            }
        }
        return probabilities[probabilities.length-1].object;
    }

}
