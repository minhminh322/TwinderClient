
import com.opencsv.CSVWriter;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class TwinderClient {

    final static private int NUMTHREADS = 100;
    final static private int NUMREQUESTS = 500000;

    public static AtomicInteger successRequest = new AtomicInteger(0);
    public static AtomicInteger unSuccessRequest = new AtomicInteger(0);

    public static CopyOnWriteArrayList<Long> latencyList = new CopyOnWriteArrayList<>();

    public static CSVWriter csvWrite;

    public static void main(String[] args) throws IOException, InterruptedException {

        BlockingQueue<HttpResponse<String>> sharedQueue = new ArrayBlockingQueue<>(10000);

        final ExecutorService producers = Executors.newFixedThreadPool(1000);
        final ExecutorService consumers = Executors.newFixedThreadPool(1000);
        final Executor newThread = Executors.newSingleThreadExecutor();

        // Create CSV log

        csvWrite = new CSVWriter(new FileWriter("test.csv"));
        csvWrite.writeNext(new String[]{"startTime", "requestType", "latency", "responseCode"});


        for (int i = 0; i < NUMTHREADS; i++) {
            producers.submit(new Producer(sharedQueue, i, NUMREQUESTS/NUMTHREADS));
            consumers.submit(new Consumer(sharedQueue, i, NUMREQUESTS));
        }
        producers.shutdown();
        consumers.shutdown();

        long startTime = System.currentTimeMillis();

        NewThread myNewThread = new NewThread(latencyList);
        newThread.execute(myNewThread);

        while (!producers.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
//            System.out.println("Completed Requests: " + successRequest);
        }

        myNewThread.running = false;

//        csvWrite.close();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("Number of Successful Requests: " + successRequest);
        System.out.println("Number of Unsuccessful Requests: " + unSuccessRequest);
        System.out.println("Total Run Time (Wall time): " + totalTime + " ms");
        System.out.println("Total Throughput: " + NUMREQUESTS/(totalTime/1000) + " requests per second");

        System.out.println("--------------------");
        DescriptiveStatistics ds = new DescriptiveStatistics();
        Iterator<Long> it = latencyList.iterator();
        while (it.hasNext())
            ds.addValue(it.next());

        double mean = ds.getMean();
        double minResponse = ds.getMin();
        double maxResponse = ds.getMax();

        System.out.println("GET methods Mean Response Time: " + mean + " ms");
        System.out.println("GET methods MIN Response Time: " + minResponse + " ms");
        System.out.println("GET methods MAX Response Time: " + maxResponse + " ms");
        csvWrite.close();

        System.out.println("--------------------");
        TwinderClient_Part2.main(null);
    }


}