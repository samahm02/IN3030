
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Oblig5 {
    private Semaphore waitSemaphore = new Semaphore(0);
    private Semaphore signalSemaphore = new Semaphore(1);
    private Semaphore countSemaphore = new Semaphore(1);
    private Semaphore entrySemaphore = new Semaphore(2); 
    private int count = 0;
    public List<Integer> inList = new ArrayList<>();
    public List<Integer> outList = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        Oblig5 ob= new Oblig5();
        for (int i = 1; i <= 10; i++) {
            final int threadId = i;
            new Thread(() -> {
                try {
                    ob.waitAndSwap(threadId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        TimeUnit.MILLISECONDS.sleep(1500);
        if (ob.TestwaitAndSwap(ob.inList, ob.outList)){
            System.out.println("waitAndSwap works as expected");
        }
        else{
            System.err.println("Error in waitAndSwap");
            System.err.println(-1);
        }
       
        System.out.println(ob.inList.toString());
        System.out.println(ob.outList.toString());

        
    }
    
    public boolean TestwaitAndSwap (List<Integer> inList,List<Integer> outList){
        for (int i = 1; i <= inList.size(); i++) {
            if (i % 2 == 1){
                if (inList.get(i-1) != outList.get(i)) {
                    return false;
                }
            }
            else if (i>0) {
                if (inList.get(i-1) != outList.get(i-2)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void waitAndSwap(int threadId) throws InterruptedException {
        entrySemaphore.acquire(); //only two threads can enter at a time
        countSemaphore.acquire();
        int myTurn = ++count;
        inList.add(threadId);
        countSemaphore.release();

        if (myTurn % 2 == 1) { // Odd threads wait
            waitSemaphore.acquire(); // Wait until an even thread releases them;
            signalSemaphore.release(); // Let the next even thread proceed
        } else { // Even threads do not wait
            signalSemaphore.acquire();
            waitSemaphore.release(); // Release the last odd thread
        }

        countSemaphore.acquire();
        outList.add(threadId);
        countSemaphore.release();
        entrySemaphore.release();
    }

    
}
