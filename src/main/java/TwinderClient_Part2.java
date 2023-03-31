import com.opencsv.CSVReader;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.FileReader;
import java.io.IOException;

public class TwinderClient_Part2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        DescriptiveStatistics ds = new DescriptiveStatistics();
        try {
            FileReader filereader = new FileReader("test.csv");
            CSVReader csvReader = new CSVReader(filereader);
            // Skip header
            csvReader.readNext();
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
//                System.out.println(nextRecord[2]);
                ds.addValue(Double.parseDouble(nextRecord[2]));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        double mean = ds.getMean();
        double median = ds.getMean();
        double p99 = ds.getPercentile(99);
        double minResponse = ds.getMin();
        double maxResponse = ds.getMax();

        System.out.println("POST method Mean Response Time: " + mean + " ms");
        System.out.println("POST method Median Response Time: " + median + " ms");
        System.out.println("POST method p99 Response Time: " + p99 + " ms");
        System.out.println("POST method MIN Response Time: " + minResponse + " ms");
        System.out.println("POST method MAX Response Time: " + maxResponse + " ms");
    }
}
