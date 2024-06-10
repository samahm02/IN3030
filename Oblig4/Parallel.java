public class Parallel {
    private int[] xPoints, yPoints;
    private IntList pointsIndexes;
    private ConvexHull convexHull;

    private int minIndex, maxIndex;

    public Parallel(int[] xPoints, int[] yPoints, IntList pointsIndexes) {
        this.xPoints = xPoints;
        this.yPoints = yPoints;
        this.pointsIndexes = pointsIndexes;
    }

    // Computes the convex hull using parallel processing
    public ConvexHull computeConvexHull(int threads) {
        int maxDepth = (int) (Math.log(threads) / Math.log(2));
        this.convexHull = new ConvexHull(xPoints, yPoints, new IntList());
        
        minIndex = findMinXIndex();
        maxIndex = findMaxXIndex();
        // Create worker threads for the left and right halves of the point set
        HullWorker leftWorker = new HullWorker(xPoints, yPoints, pointsIndexes, minIndex, maxIndex, maxDepth);
        HullWorker rightWorker = new HullWorker(xPoints, yPoints, pointsIndexes, maxIndex, minIndex, maxDepth);
        
        Thread leftThread = new Thread(leftWorker);
        Thread rightThread = new Thread(rightWorker);
        
        leftThread.start();
        rightThread.start();
        
        try {
            leftThread.join();
            rightThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Merge the results to form the complete convex hull
        mergeResults(leftWorker, rightWorker, minIndex, maxIndex);
    
        return convexHull;
    }
    

    private void mergeResults(HullWorker leftWorker, HullWorker rightWorker, int minIndex, int maxIndex) {
        IntList leftResults = leftWorker.getResult();
        IntList rightResults = rightWorker.getResult();
    
        // Add the smallest x-value 
        convexHull.lst.add(minIndex);
  
        for (int i = leftResults.len - 1; i >= 0; i--) { // Reverse order to get in counter-clockwise order
            int index = leftResults.get(i);
            if (index != minIndex && index != maxIndex && index != 0) { // Check if index is not a placeholder or min/max.
                convexHull.lst.add(index);
            }
        }
    
        // Add the largest x-value 
        convexHull.lst.add(maxIndex);
    
        // Add all points from the right hull.
        for (int i = rightResults.len - 1; i >= 0; i--) { // Reverse order
            int index = rightResults.get(i);
            if (index != minIndex && index != maxIndex && index != 0) {
                convexHull.lst.add(index);
            }
        }
    
    }

    private int findMinXIndex() {
        int index = 0;
        for (int i = 1; i < xPoints.length; i++) {
            if (xPoints[i] < xPoints[index]) {
                index = i;
            }
        }
        return index;
    }

    private int findMaxXIndex() {
        int index = 0;
        for (int i = 1; i < xPoints.length; i++) {
            if (xPoints[i] > xPoints[index]) {
                index = i;
            }
        }
        return index;
    }



    class HullWorker implements Runnable {
        private int[] xPoints, yPoints;
        private IntList pointsIndexes;
        private IntList hullIndexes = new IntList();
        private int startIndex, endIndex;
        private int depth;
    
        public HullWorker(int[] xPoints, int[] yPoints, IntList pointsIndexes, int startIndex, int endIndex, int maxDepth) {
            this.xPoints = xPoints;
            this.yPoints = yPoints;
            this.pointsIndexes = pointsIndexes;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.depth = maxDepth;
        }
    
        
        @Override
        public void run() {
            // Check if parallel processing is needed based on the subset size and depth
            if (depth > 0 && pointsIndexes.size() > 10) {
                performParallelHullComputation();
            } else {
                // If not , perform sequential computation
                Sequential seq = new Sequential(xPoints, yPoints, pointsIndexes);
                seq.findHullSegment(pointsIndexes, startIndex, endIndex);
                synchronized (this) {
                    hullIndexes = seq.hullIndexes;
                }
            }
        }
    
        private void performParallelHullComputation() {
            int[] lineEquation = Sequential.calculateLineEquation(startIndex, endIndex);
            int a = lineEquation[0], b = lineEquation[1], c = lineEquation[2];
            // lists to store points on the left and right sides of the line
            IntList leftOfLine = new IntList();
            IntList rightOfLine = new IntList();
    
            for (int i = 0; i < pointsIndexes.size(); i++) {
                int index = pointsIndexes.get(i);
                if (index == startIndex || index == endIndex) continue;
    
                int distance = a * xPoints[index] + b * yPoints[index] + c;
                if (distance > 0) {
                    leftOfLine.add(index);
                } else if (distance < 0) {
                    rightOfLine.add(index);
                }
            }
            // Spawn new threads if the set of points is large enough
            if (depth > 1 && leftOfLine.size() > 10 && rightOfLine.size() > 10) {
                HullWorker leftWorker = new HullWorker(xPoints, yPoints, leftOfLine, startIndex, endIndex, depth - 1);
                HullWorker rightWorker = new HullWorker(xPoints, yPoints, rightOfLine, startIndex, endIndex, depth - 1);
                Thread leftThread = new Thread(leftWorker);
                Thread rightThread = new Thread(rightWorker);
    
                leftThread.start();
                rightThread.start();
    
                try {
                    leftThread.join();
                    rightThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
    
                hullIndexes.append(leftWorker.getResult());
                hullIndexes.append(rightWorker.getResult());
            } else {
                // Handle remaining points sequentially to avoid overhead
                Sequential seqLeft = new Sequential(xPoints, yPoints, leftOfLine);
                seqLeft.findHullSegment(leftOfLine, startIndex, endIndex);
                Sequential seqRight = new Sequential(xPoints, yPoints, rightOfLine);
                seqRight.findHullSegment(rightOfLine, startIndex, endIndex);
    
                hullIndexes.append(seqLeft.hullIndexes);
                hullIndexes.append(seqRight.hullIndexes);
            }
        }
    
        public IntList getResult() {
            return hullIndexes;
        }
    }
    
}