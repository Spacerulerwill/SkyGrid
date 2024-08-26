package net.spacerulerwill.skygrid.util;

import net.minecraft.util.math.random.Random;

import java.util.List;

public class ProbabilityTable<T> {
    public static final class Probability<T> {
        private final T object;
        private double weight;

        public Probability(T object, double weight) {
            this.object = object;
            this.weight = weight;
        }

        public double getWeight() {
            return this.weight;
        }
    }

    private final List<Probability<T>> probabilities;
    private final int[] aliases;

    public ProbabilityTable(List<Probability<T>> probabilities) {
        int n = probabilities.size();
        this.probabilities = probabilities;
        this.aliases = new int[n];

        // Sum of weights
        double sum = probabilities.stream().map(Probability::getWeight).reduce(0.0, Double::sum);

        // Scale weights by the number of objects
        double[] scaledWeights = new double[n];
        for (int i = 0; i < n; i++) {
            scaledWeights[i] = (this.probabilities.get(i).weight * n) / sum;
        }

        // Separate into small and large
        int smallCount = 0;
        int[] small = new int[n];
        int largeCount = 0;
        int[] large = new int[n];

        for (int i = 0; i < n; i++) {
            if (scaledWeights[i] < 1.0) {
                small[smallCount++] = i;
            } else {
                large[largeCount++] = i;
            }
        }

        // Process the small and large lists
        while (smallCount > 0 && largeCount > 0) {
            int smallIndex = small[--smallCount];
            int largeIndex = large[--largeCount];

            this.probabilities.get(smallIndex).weight = scaledWeights[smallIndex];
            this.aliases[smallIndex] = largeIndex;

            scaledWeights[largeIndex] = scaledWeights[largeIndex] + scaledWeights[smallIndex] - 1.0;

            if (scaledWeights[largeIndex] < 1.0) {
                small[smallCount++] = largeIndex;
            } else {
                large[largeCount++] = largeIndex;
            }
        }

        // Fill in the remaining entries
        while (largeCount > 0) {
            this.probabilities.get(large[--largeCount]).weight = 1.0;
        }
        while (smallCount > 0) {
            this.probabilities.get(small[--smallCount]).weight = 1.0;
        }
    }

    public T sample(Random random) {
        int column = random.nextInt(this.probabilities.size());
        boolean coinToss = random.nextDouble() < this.probabilities.get(column).weight;
        return coinToss ? this.probabilities.get(column).object : this.probabilities.get(aliases[column]).object;
    }
}
