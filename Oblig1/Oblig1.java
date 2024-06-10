import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Oblig1 {
    static CyclicBarrier barrier;
    public static void main(String[] args) {
        if (args.length != 2){
            System.out.println(" Need more parameters!");
            System.exit(1);
        }
        
        int n = Integer.parseInt(args[0]);
        int k = Integer.parseInt(args[1]);

        Random r = new Random(7363);

        // makes an random array of n
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = r.nextInt(n);
        }

        double medianTimesA1 = timeAlgorithm(() -> a1(arr.clone(), k), 7);
        double medianTimesA2 = timeAlgorithm(() -> a2(arr.clone(), k), 7);
        // set to 8 threads for the 8 cores
        double medianTimesA3 = timeAlgorithm(() -> runParallel(arr.clone(), k,8), 7);

        int[] testPara =runParallel(arr.clone(),k,2);
        
        if (!compareArr(a1(arr, k),testPara,k)){
            System.out.println(" Sorted Arrays Dont Match Para");
            System.exit(-1);
        }

         if (!compareArr(a1(arr, k),a2(arr, k),k)){
            System.out.println(" Sorted Arrays Dont Match A2");
            System.exit(-1);
        }
        System.out.println("Median time taken for a1: " + medianTimesA1 + " ms");
        System.out.println("Median time taken for a2: " + medianTimesA2 + " ms");
        System.out.println("Median time taken for parallel: " + medianTimesA3 + " ms");

        //calculate sppedup
        double speedup = medianTimesA2/medianTimesA3;
        System.out.println("Speedup is: "+ speedup);

    }

    //code from the oblig, just changed to make it desc
    private static void insertSortDescending(int[] a, int v, int h) {
        int i, t;
        for (int k = v; k <= h; k++) {
            t = a[k];
            i = k - 1;
            while (i >= v && a[i] < t) {
                a[i + 1] = a[i];
                i--;
            }
            a[i + 1] = t;
        }
    }

    //made based on the steps in the assignment
    private static int[] a2 (int[] arr, int k){
        //step 1
        insertSortDescending(arr, 0, k-1);
        //step 2
        int smallest = k-1;
        for (int j=k; j< arr.length; j++){
            if (arr[j]> arr[smallest]){
                //step 2a
                int swap = arr[smallest];
                arr[smallest] = arr[j];
                arr[j] = swap;
                //step 2b
                insertSortDescending(arr, 0, k - 1);
            }
        }

        return arr;
    }
    // Sorts array using .sort and gets the k biggets elements
    private static int[] a1(int[] arr, int k) {
        Arrays.sort(arr);
        int[] result = new int[k];
        for (int i = 0; i < k; i++) {
            result[i] = arr[arr.length - 1 - i];
        }

        return result;
    }
    // compares the first k elements of 2 arrays
    private static boolean compareArr(int[] arr1, int[] arr2, int k) {
        for (int i = 0; i < k; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }
    //metod that runs an algoritm x times and returns the median time
    private static double timeAlgorithm(Runnable metod, int iterations) {
        double[] times = new double[iterations];
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            metod.run();
            long endTime = System.nanoTime();
            times[i] = (endTime - startTime) / 1000000;
        }
        Arrays.sort(times);
        return times[iterations / 2]; // median of sorted times
    }
    private static int[] runParallel(int[] arr, int k, int thr) {
        int part = arr.length / (thr + 1); // split the array into (thr + 1) parts, so that the main can also sort
        barrier = new CyclicBarrier(thr + 1);

        // make and start threads for each part of the array
        for (int i = 0; i < thr; i++) {
            int start = i * part;
            int stop = (i + 1) * part;
            new Thread(new Parallel(arr, start, stop, k)).start();
        }
        
        // Main workes of the last part of the array to the end
        int mainStart = thr * part;
        int mainStop = arr.length;

        //Here is where the main tread sorts its part
        //first sorts the k first in its part of the array
        insertSortDescending(arr, mainStart, mainStart + k - 1);
        
        for (int i = mainStart + k; i < mainStop; i++) {
            if (arr[i] > arr[mainStart + k - 1]) {
                int swap = arr[i];
                arr[i] = arr[mainStart + k - 1];
                arr[mainStart + k - 1] = swap;
                insertSortDescending(arr, mainStart, mainStart + k - 1);
            }
        }

        // Wating on the other treads
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // This iterates through the parts of the array again, 
        //making swaps if elements found in later parts of the array are larger than the elements in k first.
        for (int i = 1; i < thr+1; i++) {
            for (int j = i * part; j < (i * part + k); j++) {
                if (arr[j] >= arr[k - 1]) {
                    int swap = arr[j];
                    arr[j] = arr[k - 1];
                    arr[k - 1] = swap;
                    sortOrder(arr, k);
                }
            }
        }
        
        return arr;
    }

    // this makes sure that the first k are sorted in descending order.
    private static void sortOrder(int[] arr, int k) {
        for (int i = k - 1; i > 0; i--) {
            if (arr[i] > arr[i - 1]) {
                int swap = arr[i];
                arr[i] = arr[i- 1];
                arr[i- 1] = swap;
            }
        }
    }
    static class Parallel implements Runnable {
        private final int[] arr;
        private final int start;
        private final int stop;
        private final int k;
    
        Parallel(int[] arr, int start, int stop, int k) {
            this.arr = arr;
            this.start = start;
            this.stop = stop;
            this.k = k;
        }
    
        @Override
        public void run() {
            //Here is where the treads sorts its part
            insertSortDescending(arr,start, start + k - 1);
            
            for (int i = start + k; i <= stop; i++) {
                if (arr[i] > arr[start + k - 1]) {
                    int swap = arr[i];
                    arr[i] = arr[start + k - 1];
                    arr[start + k - 1] = swap;
                    sortOrderThr();
                }
            }
    
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
        // same as sortOrder but just in the firt k in its part of the array
        private void sortOrderThr() {
            for (int i = start + k - 1; i > start; i--) {
                if (arr[i] > arr[i - 1]) {
                    int swap = arr[i];
                    arr[i] = arr[i- 1];
                    arr[i- 1] = swap;
                }
            }
        }    
    }
}

