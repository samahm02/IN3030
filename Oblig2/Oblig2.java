public class Oblig2 {
    public static void main(String[] args) {
        if (args.length != 1){
            System.out.println(" Need paramenter 'n' for matrix size!");
            System.exit(1);
        }
        int n = Integer.parseInt(args[0]);
        //make matrixes
        double[][] A = Oblig2Precode.generateMatrixA(7363, n);
        double[][] B = Oblig2Precode.generateMatrixB(7363, n);

        // Sequential
        long start = System.nanoTime();
        double[][] C_classic = classic(A, B, n);
        long end = System.nanoTime();
        System.out.println("Classic: " + (end - start) / 1000000 + " ms");

        start = System.nanoTime();
        double[][] C_B_transposed = B_transposed(A, B, n);
        end = System.nanoTime();
        System.out.println("B Transposed: " + (end - start) / 1000000 + " ms");

        start = System.nanoTime();
        double[][] C_A_transposed = A_transposed(A, B, n);
        end = System.nanoTime();
        System.out.println("A Transposed: " + (end - start) / 1000000 + " ms");
        // Parallel
        start = System.nanoTime();
        double[][] C_Classic_Para = classic_Para(A, B, n);
        end = System.nanoTime();
        System.out.println("Classic Parallel: " + (end - start) / 1000000 + " ms");

        start = System.nanoTime();
        double[][] C_B_transposed_Para = B_transposed_Para(A, B, n);
        end = System.nanoTime();
        System.out.println("B Transposed Parallel: " + (end - start) / 1000000 + " ms");

        start = System.nanoTime();
        double[][] C_A_transposed_Para = A_transposed_Para(A, B, n);
        end = System.nanoTime();
        System.out.println("A Transposed Parallel: " + (end - start) / 1000000 + " ms");

        //test if they are equal
        if (Equal(C_classic, C_A_transposed, n) && Equal(C_classic, C_B_transposed, n) && Equal(C_classic, C_Classic_Para, n) && Equal(C_classic, C_A_transposed_Para, n) && Equal(C_classic, C_B_transposed_Para, n) ){

        }
        else{
            System.out.println("Matrixes dont mantch");
            System.exit(1);
        }
    }

    //test if 2 matrixes are equal with a litte difference
    public static boolean Equal(double[][] a, double[][] b, int n) {
        final double difference = 1E-9;
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > difference) {
                    return false;
                }
            }
        }
        return true;
    }

    // The normal way of multiplication
    public static double[][] classic(double a[][],double b[][], int n ) {
        double[][] C = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = 0;
                for (int k = 0; k < n; k++) {
                    C[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return C;
        
    }

    // flips the given matrix
    public static double[][] transposeMatrix(double[][] matrix, int n) {
        double[][] transposedMatrix = new double[n][n];
    
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                transposedMatrix[i][j] = matrix[j][i];
            }
        }
        
        return transposedMatrix;
    }

    // almost same as the normal way but have swapped indekses
    public static double[][] A_transposed(double a[][],double b[][], int n ) {
        double[][] C = new double[n][n];

        a=transposeMatrix(a, n);
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = 0;
                for (int k = 0; k < n; k++) {
                    C[i][j] += a[k][i] * b[k][j];
                }
            }
        }
        return C;
        
    }

    // almost same as the normal way but have swapped indekses
    public static double[][] B_transposed(double a[][],double b[][], int n ) {
        double[][] C = new double[n][n];

        b=transposeMatrix(b, n);
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = 0;
                for (int k = 0; k < n; k++) {
                    C[i][j] += a[i][k] * b[j][k];
                }
            }
        }
        
        return C;
    }

    // start the threads 
    public static double[][] parallelMultiply(double[][] a, double[][] b, Oblig2Precode.Mode type, int n) {
        double[][] c = new double[n][n];
        int cores = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[cores];

        // split the matrixes into parts of each thread
        int rowsPerThread = n / cores;
        int startRow = 0;
        for (int i = 0; i < cores; i++) {
            int endRow = startRow + rowsPerThread;
            if (i == cores - 1) {
                // handles the final part
                endRow = n;
            }
            final int finalStartRow = startRow;
            final int finalEndRow = endRow;
            threads[i] = new Thread(() -> {
                Parallel task = new Parallel(a, b, c, finalStartRow, finalEndRow, type);
                task.run();
            });
            threads[i].start();
            startRow = endRow;
        }
    
        
        //wait for all threads to complete and join
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        return c;
    }

    // metods for starting the parallel and transposing before if needed
    static double[][] classic_Para(double[][] a, double[][] b,int n) {
        return parallelMultiply(a, b, Oblig2Precode.Mode.PARA_NOT_TRANSPOSED, n);
    }

    static double[][] A_transposed_Para(double[][] a, double[][] b, int n) {
        a = transposeMatrix(a, n);
        return parallelMultiply(a, b, Oblig2Precode.Mode.PARA_A_TRANSPOSED, n);
    }

    static double[][] B_transposed_Para(double[][] a, double[][] b, int n) {
        b = transposeMatrix(b, n);
        return parallelMultiply(a, b, Oblig2Precode.Mode.PARA_B_TRANSPOSED, n);
    }

    static class Parallel implements Runnable {
        private final double[][] a;
        private final double[][] b;
        private double[][] c;
        private final int startRow;
        private final int endRow;
        private final Oblig2Precode.Mode type;

        public Parallel(double[][] a, double[][] b, double[][] c, int startRow, int endRow, Oblig2Precode.Mode type) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.startRow = startRow;
            this.endRow = endRow;
            this.type = type;
        }

        @Override
        public void run() {
            // preformes multiplication based on type
            switch (type) {
                case PARA_NOT_TRANSPOSED:
                    multiplyClassic();
                    break;
                case PARA_A_TRANSPOSED:
                    multiplyATransposed();
                    break;
                case PARA_B_TRANSPOSED:
                    multiplyBTransposed();
                    break;
            }
        }

        //methods for multiplication
        // same as the sequential ones but limitied to their parts
        private void multiplyClassic() {
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < c.length; j++) {
                    for (int k = 0; k < a[0].length; k++) {
                        c[i][j] += a[i][k] * b[k][j];
                    }
                }
            }
        }
    
        private void multiplyATransposed() {
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < c.length; j++) {
                    for (int k = 0; k < a[0].length; k++) {
                        c[i][j] += a[k][i] * b[k][j]; 
                    }
                }
            }
        }
    
        private void multiplyBTransposed() {
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < c.length; j++) {
                    for (int k = 0; k < a[0].length; k++) {
                        c[i][j] += a[i][k] * b[j][k]; 
                    }
                }
            }
        }
    }
}
