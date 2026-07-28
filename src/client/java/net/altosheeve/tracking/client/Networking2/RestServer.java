package net.altosheeve.tracking.client.Networking2;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpResponse;

public class RestServer {

    public static final String caligulaHost = "97.224.101.131";

    private HttpServer restServer;

    public RestServer(int port) throws IOException {

        restServer = HttpServer.create(new InetSocketAddress(port), 0);

        restServer.createContext("/connect", new ConnectionCreation());

        restServer.setExecutor(null);

    }

    public void start() {

        restServer.start();

        System.out.println("Rest server now active");

    }

    public static class ConnectionCreation implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String hostAddress = exchange.getRemoteAddress().getAddress().getHostAddress();

            System.out.println(hostAddress + " attempting connection");

            String headers = "{'addr' : " + hostAddress + "}";

            HttpResponse<String> verificationReq;

            try {

                System.out.println("trying");

                verificationReq = Request.get("http://" + caligulaHost + "/verified", headers);

            } catch (InterruptedException e) {
                System.out.println(e);
                throw new RuntimeException(e);
            }

            System.out.println("response: " + verificationReq.statusCode());

            if (verificationReq.statusCode() != 200) {

                exchange.sendResponseHeaders(401, 0);
                exchange.close();

                return;

            }

            Connection existingConnection = Relay.getConnection(hostAddress);
            System.out.println(existingConnection == null);

            if (existingConnection == null) {

                System.out.println("new connection");

                Connection newConnection = new Connection(hostAddress, Relay.UDPport);
                Relay.connections.add(newConnection);

                System.out.println("Verified and started " + exchange.getRemoteAddress().getAddress().getHostAddress());

            }

            exchange.sendResponseHeaders(200, 0);
            exchange.close();

        }
    }

}
