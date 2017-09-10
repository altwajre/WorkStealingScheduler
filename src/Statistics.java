/**
 * Created by Anna Bonaldo on 08/09/2017.
 */
public class Statistics {
    public String[] input = new String[3];
    public schedulerStatistics concStats = new schedulerStatistics();
    public sequentialStatistics  seqStats = new sequentialStatistics();

}
class serverStatistics {
    public int numTaskletInitiations = 0;
    public int numTaskletSteals = 0;
    public long CPUtime = 0;
    public long ClockTime = 0;
}

class schedulerStatistics {
    public int numServers = 0;
    public long totalCPUTime = 0;
    public long totalClockTime = 0;
    public int  totalSteals = 0;
    public int  totalInit = 0;
}

class sequentialStatistics {

    public int numTaskletInitiations = 0;
    public int numTaskletSteals = 0;
    public long wallClockTime = 0;
    public long CPUtime = 0;

}



