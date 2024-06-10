import java.util.ArrayList;
import java.util.Arrays;

public class Oblig3 {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Need one paramenter N, bigger than 16!");
            return;
        }
        
        int n = Integer.parseInt(args[0]);
        int k;

        if (n <= 16) {
            System.out.println("N must be greater than 16.");
            return;
        }

        if (args.length == 1 || Integer.parseInt(args[1]) == 0) {
            k = Runtime.getRuntime().availableProcessors();
        } else {
            k = Integer.parseInt(args[1]);
        }

        Oblig3Precode precode = new Oblig3Precode(n);
        Oblig3Precode precodePara = new Oblig3Precode(n);
        runAndCompare(n, k, 1, precode, precodePara);

        // comment out the one you dont want to write out
        // now it is set to write out the factors for the parallel factorization

        //precode.writeFactors();
        precodePara.writeFactors();
        

    }

    //Method that runs sequential factorization
    public static void runSeqFactorize(int n, int [] primes, Oblig3Precode precode) {
        long startValue = (long)n * n - 100;
        for (long i = startValue; i < (long)n * n; i++) {
            ArrayList<Long> factors = Factorization.sequentialFactorize(i, primes);
            for (Long factor : factors) {
                precode.addFactor(i, factor);
            }
        }
    }
    //Method that runs parallel factorization
    public static void runParaFactorize(int n,int k,int [] primes, Oblig3Precode precode) {
        long startValue = (long)n * n - 100;
        for (long i = startValue; i < (long)n * n; i++) {
            ArrayList<Long> factors = Factorization.parallelFactorize(i, primes,k);
            for (Long factor : factors) {
                precode.addFactor(i, factor);
            }
        }
    }

    // tests if 2 arrays are equal
    public static boolean areListsEqual(int[] seq, int[] parallel) {
        // test if equal lengt
        if (seq.length != parallel.length) {
            System.out.println("Actual lenght: " + seq.length);
            System.out.println("lenght i got: " + parallel.length);
            return false;
        }

        // test each element
        for (int i = 0; i < seq.length; i++) {
            if (seq[i] != parallel[i]) {
                return false;
            }
        }

        return true;
    }

    //metod that runs the alorithims "iterations" times, takes time, and compares using the areListsEqual method
    private static void runAndCompare(int n, int k, int iterations,Oblig3Precode precode, Oblig3Precode precode2 ) {
        double[] seqTimes = new double[iterations];
        double[] seqFacTimes = new double[iterations];
        double[] parTimes = new double[iterations];
        double[] parFacTimes = new double[iterations];

        for (int i = 0; i < iterations; i++) {
            // Time and run sequential algorithm for Soe
            long startSeq = System.nanoTime();
            int[] seq = Soe.sequentialSoe(n);
            long endSeq = System.nanoTime();
            seqTimes[i] = (endSeq - startSeq) / 1000000;


            // Time and run sequential algorithm for factoization
            long startFac = System.nanoTime();
            runSeqFactorize(n,seq,precode);
            long endFac = System.nanoTime();
            seqFacTimes[i] = (endFac - startFac) / 1000000;

            // Time and run parallel algorithm for soe
            long startPar = System.nanoTime();
            int[] parallel = Soe.runParallelSieve(n, k);
            long endPar = System.nanoTime();
            parTimes[i] = (endPar - startPar) / 1000000;

            // Time and run parallel algorithm for factoization
            long startFacPar = System.nanoTime();
            runParaFactorize(n,k,parallel,precode2);
            long endFacPar = System.nanoTime();
            parFacTimes[i] = (endFacPar - startFacPar) / 1000000;


            // Compare results of this iteration
            if (!areListsEqual(seq, parallel)) {
                System.err.println("Error: Lists are not equal for Soe.");
                System.exit(-1);
            }
        }

        
        Arrays.sort(seqTimes);
        Arrays.sort(seqFacTimes);
        Arrays.sort(parTimes);
        Arrays.sort(parFacTimes);
            
        
        System.out.println("Median Sequential time for Sieve of Eratosthenes: " + seqTimes[iterations/2] + " ms");
        System.out.println("Median Sequential time for Factorization: " + seqFacTimes[iterations/2] + " ms");

        System.out.println("Median Parallell time for Sieve of Eratosthenes: " + parTimes[iterations/2] + " ms");
        System.out.println("Median Parallell time for Factorization: " + parFacTimes[iterations/2] + " ms");
        

    }

}
