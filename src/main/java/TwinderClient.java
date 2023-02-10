
import com.opencsv.CSVWriter;

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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class TwinderClient {
    final static private int NUMTHREADS = 200;
    final static private int NUMREQUESTS = 500000;

    public static AtomicInteger successRequest = new AtomicInteger(0);
    public static AtomicInteger unSuccessRequest = new AtomicInteger(0);

    public static CSVWriter csvWrite;

    public static void main(String[] args) throws IOException, InterruptedException {

        BlockingQueue<HttpResponse<String>> sharedQueue = new ArrayBlockingQueue<>(1000);

        final ExecutorService producers = Executors.newFixedThreadPool(100);
        final ExecutorService consumers = Executors.newFixedThreadPool(100);

        // Create CSV log

        csvWrite = new CSVWriter(new FileWriter("test-2.csv"));
        csvWrite.writeNext(new String[]{"startTime", "requestType", "latency", "responseCode"});

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < NUMTHREADS; i++) {
            producers.submit(new Producer(sharedQueue, i, NUMREQUESTS/NUMTHREADS));
            consumers.submit(new Consumer(sharedQueue, i, NUMREQUESTS));
        }
        producers.shutdown();
        consumers.shutdown();
        while (!producers.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)) {
            System.out.println("Completed Requests: " + successRequest);
        }
        csvWrite.close();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Number of Successful Requests: " + successRequest);
        System.out.println("Number of Unsuccessful Requests: " + unSuccessRequest);
        System.out.println("Total Run Time (Wall time): " + totalTime + " ms");
        System.out.println("Total Throughput: " + NUMREQUESTS/(totalTime/1000) + " requests per second");


    }


}