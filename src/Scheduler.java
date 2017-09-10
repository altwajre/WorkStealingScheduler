import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Anna Bonaldo on 31/08/2017.
 */
public interface Scheduler {

    public void spawn(Tasklet t);
    //  Add tasklet to t.master and to queue of server t.serverIndex
    public void waitForAll(HashSet<Tasklet> master);
    //  Wait for all
    public void printStats();
}

class WorkStealingScheduler implements Scheduler {

    private static AtomicBoolean shutdownNow = new AtomicBoolean(false);
    private static ThreadLocal<Integer> serverIndex = new ThreadLocal<Integer>();
    private  ServerThread[] servers;
    public schedulerStatistics wSchedulerStats = new schedulerStatistics();


    private class ServerThread implements Runnable {
        private ThreadMXBean threadmxbean = ManagementFactory.getThreadMXBean();
        private int myIndex = 0;
        public ConcurrentLinkedDeque<Tasklet> deque = new ConcurrentLinkedDeque<>();
        private Stack<Tasklet> stack = new Stack<>();
        public  final serverStatistics stats = new serverStatistics();

        public void run() {
            shutdownNow.set(false);
            serverIndex.set(myIndex);
            long startCpuTimer = threadmxbean.getCurrentThreadCpuTime();
            long startClockTimer = System.nanoTime();
            while (!shutdownNow.get())
            {
                if(deque.isEmpty())
                    steal();

                while (!deque.isEmpty()) {
                    Tasklet t = deque.pollLast();
                    if (t != null)
                    {
                        stack.push(t);
                        Thread t1 = new Thread(t);
                        servers[myIndex].stats.numTaskletInitiations++;
                        t1.start();
                        try {
                            t1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (!t.isLeaf()) {
                            Tasklet[] children = t.getChildren();

                            if (children[0] != null) {
                                deque.addLast(children[0]);
                            }
                            if (children[1] != null) {
                                deque.addLast(children[1]);
                            }
                        }
                        stack.pop();
                    }
                }
                shutdownNow.set(checkServers());
            }
            this.stats.CPUtime = threadmxbean.getCurrentThreadCpuTime() - startCpuTimer;
            this.stats.ClockTime = startClockTimer - startCpuTimer;
        } // end run

        private boolean checkServers()
        {
            for (int i=0; i<servers.length; i++ ) {
                if (!servers[i].deque.isEmpty()||!servers[i].stack.isEmpty())
                    return false;
            }
            return true;
        }

        public boolean steal()
        {
            boolean stolen = false;
            for (int i=0; i<servers.length && (!stolen); i++ ) {
                if (i != myIndex && !servers[i].deque.isEmpty()) {
                    Tasklet t = servers[i].deque.pollFirst();
                    if (t != null) {
                        servers[myIndex].deque.addLast(t);
                        servers[myIndex].stats.numTaskletSteals++;
                        stolen = true;
                    }
                }
            }
            return stolen;
        }

        public ServerThread(int myIndex) {
            this.myIndex = myIndex;
        }

    } // end class ServerThread

    public WorkStealingScheduler(int numServers) {
        this.servers = new ServerThread[numServers];
        for (int i = 0; i < numServers; i++) {
            servers[i] = new ServerThread(i);
        }

        this.wSchedulerStats.numServers = numServers;
    }

    public void spawn(Tasklet t) {
        if (servers[0] != null)
            servers[0].deque.addLast(t);
    }

    public void waitForAll(HashSet<Tasklet> master) {
        if (master.isEmpty())
            this.shutdown();

        else {
            Thread[] serverPool=new Thread[servers.length];
            try
            {
                for (int i = 0; i < servers.length; i++) {
                    serverPool[i] = new Thread(servers[i]);
                    serverPool[i].start();
                }
                for (int i = 0; i < servers.length; i++) {
                    serverPool[i].join();
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        shutdownNow.set(true);
    }

    public void executeSequential(int[] arrayToSort) {
        QuickSort.sequentialSortArray(arrayToSort, 0, arrayToSort.length-1);
    }

    public void printStats() {
            System.out.println("server\tsteals\tinits");
            int totalSteals = 0;
            int totalInitiations = 0;
            for (int i = 0; i < servers.length; i++) {
                int numSteals = servers[i].stats.numTaskletSteals;
                int numInitiations = servers[i].stats.numTaskletInitiations;

                totalSteals += numSteals;
                totalInitiations += numInitiations;
                System.out.println(Integer.toString(i) + "\t" +
                        numSteals + "\t" + numInitiations);
            }
            System.out.println("total\t" + totalSteals + "\t" + totalInitiations);
            System.out.println("-----------------------------------------------");
    }

    public void computeStats() {

        int totalSteals = 0;
        int totalInitiations = 0;
        long totalCPUTime = 0;
        long totalClockTime = 0;
        for (int i = 0; i < servers.length; i++) {
            int numSteals = servers[i].stats.numTaskletSteals;
            int numInitiations = servers[i].stats.numTaskletInitiations;
            totalSteals += numSteals;
            totalInitiations += numInitiations;
            totalCPUTime += servers[i].stats.CPUtime;
            totalClockTime+= servers[i].stats.ClockTime;
        }
        this.wSchedulerStats.totalSteals = totalSteals;
        this.wSchedulerStats.totalInit = totalInitiations;
        this.wSchedulerStats.totalCPUTime = totalCPUTime / (this.wSchedulerStats.numServers*Runtime.getRuntime().availableProcessors());
        this.wSchedulerStats.totalClockTime = totalClockTime / (this.wSchedulerStats.numServers*Runtime.getRuntime().availableProcessors());
    }
} // end class WorkStealingScheduler
