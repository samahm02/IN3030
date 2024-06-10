/**
 * A basic class to allow the precode to compile. You will need to implement the
 * logic for finding what points make up the convex hull.
 * 
 */
public class ConvexHull {
    int n, MAX_X, MAX_Y;
    int x[], y[];
    IntList lst;

    public ConvexHull(int[] x, int[] y, IntList lst){
        this.x = x;
        this.y = y;
        n = x.length;
        this.lst = lst;
    }
    

}
