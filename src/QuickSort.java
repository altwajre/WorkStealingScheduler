import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Created by Anna Bonaldo on 31/08/2017.
 */
public class QuickSort {
    public static int[] data = null;
    public static int cutOff = 0;

    public static void sortArray (final Scheduler sched, final int[] arrayToSort, final int seqCutoff) {
        data = arrayToSort;
        cutOff = seqCutoff;
        HashSet<Tasklet> master = new HashSet<Tasklet>();
        QSortTasklet t = new QSortTasklet(master, 0, arrayToSort.length-1);
        sched.spawn(t);
        sched.waitForAll(master);

    }  //  end sortArray

    public static  int partition(int[] array, int left, int right)
    {
        int pivot = array[left];
        while (left <= right)
        {
            while (array[left] < pivot)
            {left++;}

            while (array[right] > pivot)
            {right--;}

            if (left <= right)
            {
                int tmp = array[left];
                array[left] = array[right];
                array[right] = tmp;

                left++;
                right--;
            }
        }
        return left;
    }

    public static  void sequentialSortArray(int arr[], int left, int right) {
        int index = partition(arr, left, right);
        if (left < index - 1)
            sequentialSortArray(arr, left, index - 1);
        if (index < right)
            sequentialSortArray(arr, index, right);
    }

    public static void main(String[] args)
    {
        List<String[]> input = IOFromCSVFile.readInput();
        List<Statistics> output = new ArrayList<Statistics>();

        for(int i=0; i< input.size(); i++) {
            System.out.println("Input "+i+ "||  ARRAY SIZE:"+ input.get(i)[0]+", SERVER n:"+input.get(i)[1]+", CUTOFF: "+input.get(i)[2]);
            output.add(quicksort(input.get(i)));
        }
        IOFromCSVFile.writeOutput(output);

    }

    public static void clear()
    {
        data = null;
        cutOff = 0;

    }

    public static Statistics quicksort(String[] args){
        boolean verbose = false;
        Statistics statistics = new Statistics();
        if (args.length != 3) {
            System.out.println("Usage: pqsort arr_size num_servers seq_cutoff");
            return statistics;
        }


        final int arrSize = Integer.parseInt(args[0]);
        final int numServers = Integer.parseInt(args[1]);
        final int seqCutoff = Integer.parseInt(args[2]);

        statistics.input[0] = args[0];
        statistics.input[1] = args[1];
        statistics.input[2] = args[2];

        boolean sortConcurrent = true;
        boolean sortSequential = true;

        // RANDOM ARRAY GENERATION
        int[] arrayToSort = new int[arrSize];
        Random ran = new Random((long)arrSize);

        for (int i = 0; i < arrSize; i++) {
            arrayToSort[i] = ran.nextInt(arrSize*2);
        }

        if (arrSize <= 100 && verbose) {
            for (int i = 0; i < arrSize; i++) {
                System.out.print(Integer.toString(arrayToSort[i]) + " ");
            }
            System.out.println("");
            System.out.println("");
        }


        int[] sequentialCopy = arrayToSort.clone();
        if(sortConcurrent) {

            if(verbose) System.out.println("CONCURRENT SORTING");
            WorkStealingScheduler sched = new WorkStealingScheduler(numServers);
            long startTime = System.nanoTime();
            sortArray(sched, arrayToSort, seqCutoff);
            long endTime = System.nanoTime();
            sched.shutdown();
           // sched.printStats();
            System.out.println("Total execution time: " + (endTime - startTime) );
            sched.computeStats();
            statistics.concStats= sched.wSchedulerStats;
            statistics.concStats.totalClockTime = (endTime - startTime);

        }

        if(sortSequential)
        {
            if(verbose)  System.out.println("SEQUENTIAL SORTING");
            // start timer variables
            WorkStealingScheduler sched = new WorkStealingScheduler(0);
            long startClock = System.nanoTime();
            ThreadMXBean threadmxbean = ManagementFactory.getThreadMXBean();
            threadmxbean.setThreadCpuTimeEnabled(true);
            long startCpuTimer = threadmxbean.getCurrentThreadCpuTime();
            sched.executeSequential(sequentialCopy);
            statistics.seqStats.CPUtime = threadmxbean.getCurrentThreadCpuTime() - startCpuTimer;
            statistics.seqStats.wallClockTime = System.nanoTime() -startClock;
        }

        if (arrSize <= 100 && verbose) {
            System.out.println("");
            for (int i = 0; i < arrSize; i++)
                System.out.print(Integer.toString(arrayToSort[i]) + " ");

            System.out.println('\n');

            for (int i = 0; i < arrSize; i++)
                System.out.print(Integer.toString(sequentialCopy[i]) + " ");
        }
        return statistics;
    }
} //  end class QuickSort

class QSortTasklet extends Tasklet {

    public final int start, end;
    private QSortTasklet[] children = null;


    public QSortTasklet(HashSet<Tasklet> master, int start, int end) {
        super(master);
        super.master.add(this);
        this.start = start;
        this.end = end;
    }

    public void EndTasklet()
    {
        this.master.remove(this);
    }

    @Override
    public Tasklet[] getChildren() {
        return children;
    }

    public void run() {
        long tStart = System.nanoTime();
        int[] array = QuickSort.data;

        if ((end - start + 1) > QuickSort.cutOff)
        {
            doConcurrentStuff(array);

        }
        else
        {
            QuickSort.sequentialSortArray(array, start, end);
        }

        EndTasklet();

    }

    @Override
    public boolean isLeaf() {
        return (children == null);// || (children[0] == null) || (children[1] == null);
    }

    public void doConcurrentStuff(int[] array)
    {
        if (start < end) {
            //System.out.println("start < end");
            int split_position = QuickSort.partition(array, start, end);

            children = new QSortTasklet[2];

            if (start < split_position - 1) {
                int start_a = start;
                int end_a = split_position - 1;
                //System.out.println("sto per creare figlio con " + start_a + end_a);
                QSortTasklet a = new QSortTasklet(master, start_a, end_a);
                children[0] = a;

                //System.out.println("CREATO UN FIGLIO CON START E END " + start_a + end_a);
            }

           if (end > split_position) {
                int start_b = split_position;
                int end_b = end;
                QSortTasklet b = new QSortTasklet(master, start_b, end_b);
                children[1] = b;
                //System.out.println("CREATO UN FIGLIO CON START E END " + start_b + end_b);
            }

        }
    }
} // end class QSortTasklet