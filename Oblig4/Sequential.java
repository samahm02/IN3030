 public class Sequential {
    private static int[] xPoints;
    private static int[] yPoints;
    private IntList pointsIndexes;
    public IntList hullIndexes;
    private ConvexHull convexHull;

    private int indexMinX, indexMaxX;

    public Sequential(int[] xPoints, int[] yPoints, IntList pointsIndexes) {
        this.xPoints = xPoints;
        this.yPoints = yPoints;
        this.pointsIndexes = pointsIndexes;
        this.hullIndexes = new IntList();
        this.convexHull = new ConvexHull(xPoints, yPoints, hullIndexes);
    }

    public static ConvexHull findConvexHull(int[] x, int[] y, IntList pointsIndexes) {
        Sequential sequential = new Sequential(x, y, pointsIndexes);
        sequential.computeHull();
        return sequential.convexHull;
    }

    private void computeHull() {
        indexMinX = findMinXIndex();
        indexMaxX = findMaxXIndex();
        hullIndexes.add(indexMaxX);
        findHullSegment(pointsIndexes, indexMinX, indexMaxX);
        hullIndexes.add(indexMinX);
        findHullSegment(pointsIndexes, indexMaxX, indexMinX);
    }

    public void findHullSegment(IntList candidatePoints, int startIndex, int endIndex) {
        int[] lineEquation = calculateLineEquation(startIndex, endIndex);
        int a = lineEquation[0], b = lineEquation[1], c = lineEquation[2];

        int mostLeftPoint = -1;
        int maxDistance = 0;

        IntList leftOfLine = new IntList();
        IntList onEdge = new IntList();

        for (int i = 0; i < candidatePoints.size(); i++) {
            int index = candidatePoints.get(i);
            if (index == startIndex || index == endIndex) continue;

            int distance = a * xPoints[index] + b * yPoints[index] + c;

            if (distance > 0) {
                leftOfLine.add(index);
                if (distance > maxDistance) {
                    maxDistance = distance;
                    mostLeftPoint = index;
                }
            } else if (distance == 0) {
                onEdge.add(index);
            }
        }

        if (mostLeftPoint != -1) {
            findHullSegment(leftOfLine, mostLeftPoint, endIndex);
            hullIndexes.add(mostLeftPoint);
            findHullSegment(leftOfLine, startIndex, mostLeftPoint);
        } else {
            hullIndexes.append(onEdge);
        }

    }

    public static int[] calculateLineEquation(int startIndex, int endIndex) {
        int x1 = xPoints[startIndex];
        int y1 = yPoints[startIndex];
        int x2 = xPoints[endIndex];
        int y2 = yPoints[endIndex];

        int a = y2 - y1;
        int b = x1 - x2;
        int c = x2 * y1 - x1 * y2;

        return new int[]{a, b, c};
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
} 
