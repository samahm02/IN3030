import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Factorization {
    // sequentially factorizes a given number using a list of prime numbers.
    // Returns a list of prime factors of the given number.
    public static ArrayList<Long> sequentialFactorize(long number, int[] primes) {
        ArrayList<Long> factors = new ArrayList<>();
        for (int prime : primes) {
            while (number % prime == 0) {
                factors.add((long)prime); // add prime factor to the list
                number /= prime; // sivide number by the prime factor
            }
        }
        if (number > 1) { // if the rest is larger than 1, add it as a factor
            factors.add(number);
        }
        return factors;
    }
    // Parallel factorization for a given number using a list of primes and number of threads "k".
    // each thread is responsible for a segment of the prime list to find factors of the number.
    public static ArrayList<Long> parallelFactorize(long number, int[] primes, int k) {
        ArrayList<ArrayList<Long>> partialResults = new ArrayList<>();
        CyclicBarrier barrier = new CyclicBarrier(k + 1);

        // Prepare partial results containers
        for (int i = 0; i < k; i++) {
            partialResults.add(new ArrayList<>());
        }

        Thread[] threads = new Thread[k];
        int segmentLength = primes.length / k; 

        // Start threads
        for (int i = 0; i < k; i++) {
            int start = i * segmentLength;
            int end = (i + 1) * segmentLength;
            if (i == k - 1) {
                end = primes.length; // Make sure the last thread covers the end
            }
            threads[i] = new Thread(new Parallel(number, primes, start, end, partialResults.get(i), barrier));
            threads[i].start();
        }

        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        ArrayList<Long> combinedFactors = new ArrayList<>();
        for (ArrayList<Long> partialResult : partialResults) {
            combinedFactors.addAll(partialResult);
        }

        // if a thread leaves a large prime factor
        long remainingFactor = number;
        for (Long factor : combinedFactors) {
            remainingFactor /= factor; // Reduce the remainingFactor by all found factors
        }
        if (remainingFactor > 1) { // If there's a large prime factor left
            combinedFactors.add(remainingFactor); // Add it to the list of factors
        }

        return combinedFactors;
    }
    private static class Parallel implements Runnable {

        private long number;
        private int [] primes;
        private int start, end;
        private List<Long> result;
        private CyclicBarrier barrier;

        public Parallel(long number, int [] primes, int start, int end, List<Long> result, CyclicBarrier barrier) {
            this.number = number;
            this.primes = primes;
            this.start = start;
            this.end = end;
            this.result = result;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            // factorizes the number using the assigned part of primes
            //long localNumber = number; // Work on a local copy of the number.
            for (int i = start; i < end; i++) {
                while (number % primes[i] == 0) {
                    result.add((long) primes[i]);
                    number /= primes[i];
                }
            }
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
    
}
