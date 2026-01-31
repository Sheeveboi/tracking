package net.altosheeve.tracking.client.Networking;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.altosheeve.tracking.client.Core.Relaying;
import net.altosheeve.tracking.client.Render.Rendering;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Verification {

    private static boolean verified = false;

    private static Screen parentScreen;

    private static HttpServer server;

    private static File tokenFile = new File("token.file");
    private static String accessToken = "";
    private static String refreshToken = "";
    private static String sessionToken = "";

    private static final String headers = "{\"Content-Type\": \"application/x-www-form-urlencoded\"}";
    private static final String endpoint = "https://discord.com/api/v10";
    private static final String id = "1361095535217479772";
    private static final String secret = "pB5g-nwPn76acnJCUxCe8zZHfgCx1TWL";
    private static final String verifyURL = "https://discord.com/oauth2/authorize?client_id=1361095535217479772&response_type=code&redirect_uri=http%3A%2F%2Flocalhost%3A5000&scope=identify";

    public static class VerifyScreen extends Screen {

        protected VerifyScreen(Screen parent) {
            super(Text.of("verification screen"));
            parentScreen = parent;
        }

        public ButtonWidget verifyButton = ButtonWidget.builder(
                Text.of("Click here to verify"),
                (btn) -> {
                    Util.getOperatingSystem().open(verifyURL);
                })
                .width(150)
                .build();


        @Override
        public void init() {

            verifyButton.setPosition(width / 2 - 75, height / 2 + 15);

            addDrawableChild(verifyButton);

        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {

            super.render(context, mouseX, mouseY, delta);

            Text text = Text.of("You are currently not verified by OPPWATCH_ servers!");

            int w = Rendering.client.textRenderer.getWidth(text);
            context.drawText(Rendering.client.textRenderer, text, (width / 2) - (w / 2), height / 2 - 5, 0xffffff, true);

            if (verified) Rendering.client.setScreen(parentScreen);
        }
    }

    public static void setToken() throws IOException, InterruptedException, URISyntaxException {

        if (!tokenFile.exists()) {

            Rendering.client.setScreen(new VerifyScreen(Rendering.client.currentScreen));
            startOAuth2Verification();

            return;
        }

        Scanner scanner = new Scanner(tokenFile);
        StringBuilder jsonString = new StringBuilder();
        while (scanner.hasNextLine()) jsonString.append(scanner.nextLine());
        scanner.close();

        JSONObject tokenJson = new JSONObject(jsonString.toString());

        refreshToken = tokenJson.getString("refresh_token");

        String data = "grant_type=refresh_token&refresh_token=%s".formatted(URLEncoder.encode(refreshToken));

        System.out.println("Sending oauth refresh");
        HttpResponse<String> refreshResponse = Request.post(endpoint + "/oauth2/token", headers, data, id, secret);

        System.out.println(refreshResponse.body());

        JSONObject refreshJson = new JSONObject(refreshResponse.body());

        if (!refreshJson.has("access_token")) {
            System.out.println("error in verification (no access token)");
            return;
        }
        if (!refreshJson.has("refresh_token")) {
            System.out.println("error in verification (no refresh token)");
            return;
        }

        String writeToFile = """
                    {
                        "access_token" : %s,
                        "refresh_token" : %s
                    }
                    """;
        FileWriter writer = new FileWriter(tokenFile);
        writer.write(writeToFile.formatted(refreshJson.getString("access_token"), refreshJson.getString("refresh_token")));
        writer.close();

        accessToken = refreshJson.getString("access_token");
        refreshToken = refreshJson.getString("refresh_token");

        System.out.println("sending verification request");

        HttpResponse<String> verificationResponse = Request.get("http://" + Relaying.host + "/verify?token=%s".formatted(accessToken));

        Map<String, String> verificationQuery = decodeURI(verificationResponse.uri().toString());

        if (!verificationQuery.containsKey("sessionToken")) {
            System.out.println("error in verification (Server did not return sessionToken)");
            return;
        }

        sessionToken = verificationQuery.get("sessionToken");

        verified = true;

        if (parentScreen != null) MinecraftClient.getInstance().setScreen(parentScreen);
    }

    public static boolean isVerified() { return verified; }

    public static void startOAuth2Verification() throws IOException {
        server = HttpServer.create(new InetSocketAddress(5000), 0);

        server.createContext("/", new VerificationHandler());
        server.setExecutor(null);
        server.start();
    }

    public static Map<String, String> decodeURI(String url) throws URISyntaxException {

        Map<String, String> out = new HashMap<>();

        URI uri = new URI(url);
        String query = uri.getRawQuery();
        String[] splitQuery = query.split("&");

        for (String singleQuery : splitQuery) {

            String[] splitSingleQuery = singleQuery.split("=");
            out.put(splitSingleQuery[0], URLDecoder.decode(splitSingleQuery[1], StandardCharsets.UTF_8));

        }

        return out;
    }

    static class VerificationHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                System.out.println(exchange.getRequestURI());

                Map<String, String> parameters;
                try {
                    parameters = decodeURI(exchange.getRequestURI().toString());
                } catch (URISyntaxException e) {
                    System.out.println("error in verification (URI Decoding error)");
                    return;
                }

                if (!parameters.containsKey("code")) {
                    System.out.println("error in verification (No OAuth2 Code)");
                    return;
                }

                String code = parameters.get("code");

                System.out.println(code);
                System.out.println("Sanity check");

                String data = "code=%s&grant_type=authorization_code&redirect_uri=%s".formatted(URLEncoder.encode(code), URLEncoder.encode("http://localhost:5000"));

                HttpResponse<String> response;
                try {
                    response = Request.post(endpoint + "/oauth2/token", headers, data, id, secret);
                } catch (InterruptedException e) {
                    System.out.println("error in verification (post error)");
                    throw new RuntimeException(e);
                }

                System.out.println(response.body().toString());

                JSONObject json;
                json = new JSONObject(response.body());

                System.out.println("created json object");

                if (!json.has("access_token")) {
                    System.out.println("error in verification (no access token)");
                    return;
                }
                if (!json.has("refresh_token")) {
                    System.out.println("error in verification (no refresh token)");
                    return;
                }

                boolean enable = false;
                if (!tokenFile.exists()) enable = tokenFile.createNewFile();
                if (!enable) System.out.println("unable to create file");
                System.out.println("writing file");

                String writeToFile = """
                        {
                            "access_token" : %s,
                            "refresh_token" : %s
                        }
                        """;
                FileWriter writer = new FileWriter(tokenFile);
                writer.write(writeToFile.formatted(json.getString("access_token"), json.getString("refresh_token")));
                writer.close();

                System.out.println("running");
                setToken();

                exchange.sendResponseHeaders(200, 0);
                server.stop(0);
            } catch (RuntimeException | URISyntaxException | InterruptedException e) {
                System.out.println("there was some sort of error");
                System.out.println(e);
                throw new RuntimeException(e);
            }
        }
    }
}
