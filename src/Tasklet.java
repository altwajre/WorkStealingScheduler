import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.RecursiveAction;

/**
 * Created by Anna Bonaldo on 31/08/2017.
 */
abstract class Tasklet extends RecursiveAction {
    public final HashSet<Tasklet> master;
    protected ConcurrentLinkedDeque<Tasklet> originDeque;
    //  Master object keeps set of Tasklets
    //  Tasklet is removed from its master when it completes
    //  If Master is empty at that point, notify() is called
    //  master() should only be manipulated in Scheduler or in one
    //  of its Server threads, and only in a synchronized block.

    public Tasklet(HashSet<Tasklet> master) {
        this.master = master;
    }
    abstract public boolean isLeaf();
    abstract public void addDeque (ConcurrentLinkedDeque<Tasklet> originDeque);
}