package net.altosheeve.tracking.client.Networking;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class Request extends Thread {
    private static HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public interface RequestCallback {
        void cb(Map<String, List<String>> headers, String body) throws IOException;
    }

    private static class RequestFuture extends Thread {
        private final HttpRequest request;
        private final RequestCallback cb;

        public RequestFuture(HttpRequest request, RequestCallback cb) {
            this.request = request;
            this.cb = cb;
        }

        public void run() {
            try {
                HttpResponse<String> response = client.send(this.request, HttpResponse.BodyHandlers.ofString());
                this.cb.cb(response.headers().map(), response.body());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void get(String url, RequestCallback cb) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        RequestFuture future = new RequestFuture(request, cb);
        future.start();
    }

    public static HttpResponse<String> get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void get(String url, String headers, RequestCallback cb) {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20));

        JsonParser parser = new JsonParser();
        JsonObject d = parser.parse(headers).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : d.entrySet()) request.header(entry.getKey(),entry.getValue().getAsString());

        request.GET();

        RequestFuture future = new RequestFuture(request.build(), cb);
        future.start();
    }

    public static void post(String url, RequestCallback cb) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        RequestFuture future = new RequestFuture(request, cb);
        future.start();
    }

    public static HttpResponse<String> post(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void post(String url, String headers, RequestCallback cb) {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20));

        JsonParser parser = new JsonParser();
        JsonObject d = parser.parse(headers).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : d.entrySet()) {
            request.header(entry.getKey(),entry.getValue().getAsString());
        }

        request.POST(HttpRequest.BodyPublishers.ofString(""));

        RequestFuture future = new RequestFuture(request.build(), cb);
        future.start();
    }

    public static void post(String url, String headers, String body, RequestCallback cb) {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Length", String.valueOf(body.length()));

        JsonParser parser = new JsonParser();
        JsonObject h = parser.parse(headers).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : h.entrySet()) {
            request.header(entry.getKey(),entry.getValue().getAsString());
        }

        JsonObject b = parser.parse(body).getAsJsonObject();
        request.POST(HttpRequest.BodyPublishers.ofString(b.toString()));
        //yes this is stupid but you cant trust third party json parsers for shit these days

        RequestFuture future = new RequestFuture(request.build(), cb);
        future.start();
    }

    public static void post(String url, String headers, String body, String username, String password, RequestCallback cb) {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
                .header("Content-Length", String.valueOf(body.length()));

        JsonParser parser = new JsonParser();
        JsonObject h = parser.parse(headers).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : h.entrySet()) {
            request.header(entry.getKey(),entry.getValue().getAsString());
        }

        JsonObject b = parser.parse(body).getAsJsonObject();
        request.POST(HttpRequest.BodyPublishers.ofString(b.toString()));
        //yes this is stupid but you cant trust third party json parsers for shit these days

        RequestFuture future = new RequestFuture(request.build(), cb);
        future.start();
    }

    public static HttpResponse<String> post(String url, String headers, String body, String username, String password) throws IOException, InterruptedException {

        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));

        JsonParser parser = new JsonParser();
        JsonObject h = parser.parse(headers).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : h.entrySet()) {
            request.header(entry.getKey(),entry.getValue().getAsString());
        }

        request.POST(HttpRequest.BodyPublishers.ofString(body));
        //yes this is stupid but you cant trust third party json parsers for shit these days

        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

}
