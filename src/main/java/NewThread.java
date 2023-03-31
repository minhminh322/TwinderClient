import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class NewThread implements Runnable, Configuration {
    public boolean running = true;

    private CopyOnWriteArrayList<Long> latencyList;
    public NewThread(CopyOnWriteArrayList<Long> latencyList) {
        this.latencyList = latencyList;
    }
    @Override
    public void run() {
        final HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        Gson gson = new Gson();

//        ArrayList<Long> latencies = new ArrayList<>();

        while (running) {
            try {
                for (int i=0; i < 5; i++) {
                    long startTime = System.currentTimeMillis();
                    String randomID = String.valueOf(ThreadLocalRandom.current().nextInt(1, 5000));
                    // Execute the GET
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(EC2_PATH + "stats" + "/" + randomID))
//                    .header("Content-Type", "application/json")
                            .GET()
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//                    System.out.println("Result of GET request " + ": " + response.body());
                    long latency = System.currentTimeMillis() - startTime;
                    latencyList.add(latency);
                }
//                Iterator<Long> it = latencyList.iterator();
//                while (it.hasNext())
//                    System.out.println(it.next());
                Thread.sleep(1000);
            }   catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
