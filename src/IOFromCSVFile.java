import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Anna Bonaldo on 08/09/2017.
 */


public class IOFromCSVFile {

    private static final char DEFAULT_SEPARATOR = ';';
    private static final char DEFAULT_QUOTE = '"';
    static String outputFile = "CSVFiles\\outputData.csv";
    static String inputFile = "CSVFiles\\inputData.csv";



    public static List<String[]> readInput()
    {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(inputFile));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        ArrayList<String[]> allInput = new ArrayList<String[]>();

        if (scanner != null) {
            while (scanner.hasNext()) {
            List<String> line = parseLine(scanner.nextLine());
            String[] input = new String[3];
            input[0] = line.get(0);
            input[1] = line.get(1);
            input[2] = line.get(2);
            allInput.add(input);
        }
        }
        scanner.close();
        return  allInput;
    }

    public static void writeOutput(List<Statistics> statList)
    {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new File(outputFile));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        writeHeader(pw);

        for (Statistics stat:statList) {
            writeLine(pw, stat);
        }

        pw.close();
        System.out.println("done!");
    }

    public static void writeHeader( PrintWriter pw){

        StringBuilder sb = new StringBuilder();
        sb.append("ARRAY SIZE");
        sb.append(DEFAULT_SEPARATOR);
        sb.append("Num SERVERS");
        sb.append(DEFAULT_SEPARATOR);
        sb.append("CUTOFF");
        sb.append(DEFAULT_SEPARATOR);

        sb.append("Num STEALS"); sb.append(DEFAULT_SEPARATOR);
        sb.append("Thread CPU"); sb.append(DEFAULT_SEPARATOR);
        sb.append("Thread CLOCK"); sb.append(DEFAULT_SEPARATOR);

        sb.append("SEQ CPU ");  sb.append(DEFAULT_SEPARATOR);
        sb.append("SEQ CLOCK");  sb.append(DEFAULT_SEPARATOR);

        sb.append('\n');

        pw.write(sb.toString());
    }

    public static void writeLine( PrintWriter pw, Statistics lineStats){

        StringBuilder sb = new StringBuilder();
        //"ARRAY SIZE"
        sb.append(lineStats.input[0]); sb.append(DEFAULT_SEPARATOR);
        //"Num SERVERS"
        sb.append(lineStats.input[1]); sb.append(DEFAULT_SEPARATOR);
        //"CUTOFF"
        sb.append(lineStats.input[2]); sb.append(DEFAULT_SEPARATOR);


        //"Num STEALS"
        sb.append(lineStats.concStats.totalSteals); sb.append(DEFAULT_SEPARATOR);

        //"TOTAL CPU TIME"
        sb.append(lineStats.concStats.totalCPUTime); sb.append(DEFAULT_SEPARATOR);
       //"TOTAL CLOCK TIME"
        sb.append(lineStats.concStats.totalClockTime); sb.append(DEFAULT_SEPARATOR);


        //"SEQUENTIAL CPU TIME"
        sb.append(lineStats.seqStats.CPUtime); sb.append(DEFAULT_SEPARATOR);
        //"SEQUENTIAL CLOCK TIME"
        sb.append(lineStats.seqStats.wallClockTime); sb.append(DEFAULT_SEPARATOR);

        sb.append('\n');

        pw.write(sb.toString());
    }

    public static List<String> parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }


    public static List<String> parseLine(String cvsLine, char separators, char customQuote) {

        List<String> result = new ArrayList<>();

        //if empty, return!
        if (cvsLine == null && cvsLine.isEmpty()) {
            return result;
        }

        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }

                }
            } else {
                if (ch == customQuote) {

                    inQuotes = true;

                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separators) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
    }

}
