import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class Soe {

    // Sequential version, uses the precode
    public static int[] sequentialSoe(int n) {
        SieveOfEratosthenes seq = new SieveOfEratosthenes(n);
        return seq.getPrimes();
    }

    // Parallel version of the Sieve of Eratosthenes
    public static int[] runParallelSieve(int n, int threadCount) {
        int root = (int) Math.sqrt(n);
        byte [] oddNumbers = new byte[(n / 16) + 1];
        CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);

        // Initialize the oddNumbers array
        Arrays.fill(oddNumbers, (byte) 0);
        mark(1, oddNumbers); // mark 1 as not prime.
    
        List<Integer> primesUpToRoot = new ArrayList<>();

        
        // sequentially find and mark primes up to sqrt of n.
        for (int i = 3; i <= root; i += 2) {
            if (!isPrime(i, oddNumbers)) continue;
            primesUpToRoot.add(i);
            for (int j = i * i; j <= root; j += i * 2) {
                mark(j, oddNumbers);
            }
        }

        // start threads to mark multiples of primes up to sqrt of n to the end 
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            long startLong = ((long) i * n) / threadCount + 1;
            long endLong = ((long) (i + 1) * n) / threadCount;
            int start = (int) startLong;
            int end = (int) endLong;
            threads[i] = new Thread(new Parallel(start, end, n, barrier, oddNumbers, primesUpToRoot));
            threads[i].start();
        }

        try {
            barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }


        // part that collect primes found
        int numberOfPrimes = 1; //counting from 1 to include 2 as a prime
        for (int i = 3; i <= n; i += 2) {
            if (isPrime(i, oddNumbers)) {
                numberOfPrimes++;
            }
        }
        //the reason loop through the list 2 times, one to find the lengt and then make the list
        //is because, when i used a arraylist i got out of memory error
        int[] primes = new int[numberOfPrimes];
        primes[0] = 2; 
        int primeIndex = 1;

        for (int i = 3; i <= n; i += 2) {
            if (isPrime(i, oddNumbers)) {
                primes[primeIndex++] = i;
            }
        }
        
        return primes;
    }


    //metod taken from precode
    private static boolean isPrime(int num, byte[] oddNumbers ) {
        int bitIndex = (num % 16) / 2;
        int byteIndex = num / 16;
    
        return (oddNumbers[byteIndex] & (1 << bitIndex)) == 0;
    }

    //metod taken from precode
    private static void mark(int num, byte[] oddNumbers) {
        int bitIndex = (num % 16) / 2;
        int byteIndex = num / 16;
        oddNumbers[byteIndex] |= (1 << bitIndex);
    }

    


    private static class Parallel implements Runnable {

        private int from;
        private int to;
        private CyclicBarrier barrier;
        private byte [] oddNumbers;
        private List<Integer> primes;

        public Parallel(int from, int to, int n, CyclicBarrier barrier, byte[] oddNumbers, List<Integer> primes) {
            this.from = Math.max(from, 3); // make sure that start is from at least 3
            this.to = to;
            this.barrier = barrier;
            this.oddNumbers = oddNumbers;
            this.primes = primes;
        }

        @Override
        public void run() {
            // Iterate through each prime number found up to sqrt of n
            for (int prime : primes) {
                // Start marking from max of prime^2 or nearest multiple within range
                int start = Math.max(prime * prime, ((from + prime - 1) / prime) * prime);
                if (start % 2 == 0){
                    start += prime; // make sure that start is from an odd multiple.
                }
                // Mark all odd multiples of prime within the segment.
                for (int j = start; j <= to; j += prime * 2) {
                    mark(j, oddNumbers);
                }
            }
        
            try {
                barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
