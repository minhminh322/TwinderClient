import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class Producer implements Runnable {
    private final BlockingQueue<HttpResponse<String>> sharedQueue;
    private int threadNo;
    private int numberOfRequests;
    private static final int SWIPER_MAX = 5000;
    private static final int SWIPEE_MAX = 100000;

    private static final int COMMENTS_MAX = 256;

    public Producer(BlockingQueue<HttpResponse<String>> sharedQueue, int threadNo, int numberOfRequests) {
        this.threadNo = threadNo;
        this.sharedQueue = sharedQueue;
        this.numberOfRequests = numberOfRequests;
    }
    public static String randomSwipe() {
        Random random = new Random();
        if(random.nextInt(2) == 0)
            return "LEFT";
        return "RIGHT";
    }

    public static int randomNumber(int maxNumber) {
        Random random = new Random();
        return random.nextInt(maxNumber+1);
     }
    public static String randomComment() {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        int totalLength = random.nextInt(256 + 1);
        for(int i = 0 ; i < totalLength; i++) {
        builder.append((char)(random.nextInt(26) + 'a'));
         }
         return builder.toString();
    }

    @Override
    public void run() {
        // Producer produces a continuous stream of numbers for every 200 milli seconds
        final HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        Gson gson = new Gson();
        String ec2Host = "ec2-18-206-207-70.compute-1.amazonaws.com";
        String serviceUrl = "http://" + ec2Host + ":8080/Twinder-project_war/swipe/";
//        String serviceUrl = "http://localhost:8080/Twinder_project_war_exploded/swipe/";
        for (int i = 0; i < numberOfRequests; i++) {
            RequestBody requestBody = new RequestBody();
            String swipeDirection = randomSwipe();
            int swiper = randomNumber(SWIPER_MAX);
            int swipee = randomNumber(SWIPEE_MAX);
            String comment = randomComment();

            requestBody.setSwipe(swipeDirection);
            requestBody.setSwiper(swiper);
            requestBody.setSwipee(swipee);
            requestBody.setComment(comment);
            requestBody.setStartTime(System.currentTimeMillis());

            // Execute the POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serviceUrl + swipeDirection))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .build();

            HttpResponse<String> response = null;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                sharedQueue.put(response);
                System.out.println("Producer " + this.threadNo + ": " + response.body());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
