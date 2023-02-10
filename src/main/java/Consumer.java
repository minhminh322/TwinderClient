import com.google.gson.Gson;

import java.io.Reader;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/* Different consumers consume data from shared queue, which is shared by both producer and consumer threads */
public class Consumer implements Runnable {
    private final BlockingQueue<HttpResponse<String>> sharedQueue;
    private int threadNo;
    private int numberOfRequests;
    private static Gson gson;

    public Consumer(BlockingQueue<HttpResponse<String>> sharedQueue, int threadNo, int numberOfRequests) {
        this.sharedQueue = sharedQueue;
        this.threadNo = threadNo;
        this.numberOfRequests = numberOfRequests;
        this.gson = new Gson();
    }

    private void processResponse(HttpResponse<String> res) {

        RequestBody resBody = gson.fromJson(res.body(), RequestBody.class);
//        System.out.println(resBody.getStartTime());

        long latency = System.currentTimeMillis() - resBody.getStartTime();
        Date startTime = new Date(resBody.getStartTime());
        String[] record = {startTime.toString(), "POST", String.valueOf(latency), String.valueOf(res.statusCode())};
        TwinderClient.csvWrite.writeNext(record);

    }

    @Override
    public void run() {
        // Consumer consumes numbers generated from Producer threads continuously
        while (TwinderClient.successRequest.get() < numberOfRequests) {

            try {
                HttpResponse<String> response = sharedQueue.take();
                processResponse(response);
                if (response.statusCode() == 200) {
                    TwinderClient.successRequest.getAndIncrement();
                } else {
                    TwinderClient.unSuccessRequest.getAndIncrement();
                }

                System.out.println("Consumer " + this.threadNo + ": "+ TwinderClient.successRequest.get() + " " + response.body());
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }
}
