public class Oblig4 {
    

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Need 2 paramenters: N (Number of Nodes) and Seed for generating points");
            System.out.println("Optonal paramenter for number of Threads");
            return;
        }
        
        int n = Integer.parseInt(args[0]);
        int seed = Integer.parseInt(args[1]);
        int threads;

        if (args.length == 2 || Integer.parseInt(args[2]) == 0) {
            threads = Runtime.getRuntime().availableProcessors();
        } else {
            threads = Integer.parseInt(args[2]);
        }
        
        int[] x = new int[n];
        int[] y = new int[n];

    
        NPunkter17 nPunkter17 = new NPunkter17(n, seed);
        IntList intList = nPunkter17.lagIntList();
        nPunkter17.fyllArrayer(x, y);

        long start = System.nanoTime();
        ConvexHull convexHullS = Sequential.findConvexHull(x, y, intList);
        long end = System.nanoTime();
        long seqtime = (end - start)/1000000;
        System.out.println("Sequential version took: " + seqtime + " ms");
        Oblig4Precode seqResult = new Oblig4Precode(convexHullS,convexHullS.lst);



        Parallel par= new Parallel(x, y, intList);
        long startPar = System.nanoTime();
        ConvexHull convexHullP = par.computeConvexHull(threads);
        long endPar = System.nanoTime();
        long Partime = (endPar - startPar)/1000000;
        System.out.println("Parallel version took: " + Partime + " ms");
        
        System.out.println("Speedup = " + (double) seqtime / Partime);

        Oblig4Precode parResult = new Oblig4Precode(convexHullP,convexHullP.lst);

        if (n < 10000){
            //seqResult.writeHullPoints();
            parResult.writeHullPoints();

        }
        parResult.drawGraph();
        //seqResult.drawGraph();

    }
}


