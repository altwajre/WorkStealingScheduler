import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anna Bonaldo on 09/09/2017.
 */
public class BatchQuickSortExecutors {
    public static void main(String[] args)
    {
        List<String[]> input = IOFromCSVFile.readInput();
        List<Statistics> output = new ArrayList<Statistics>();

        for(int i=0; i< input.size(); i++) {
            System.out.println("Input "+i+ "||  ARRAY SIZE:"+ input.get(i)[0]+", SERVER n:"+input.get(i)[1]+", CUTOFF: "+input.get(i)[2]);
            output.add(QuickSort.quicksort(input.get(i)));
        }
        IOFromCSVFile.writeOutput(output);

    }
}
