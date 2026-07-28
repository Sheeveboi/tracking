package net.altosheeve.tracking.client.Networking2;

import net.altosheeve.tracking.client.Networking2.NetworkObjects.Player;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class ClientTest {

    public static void main(String[] args) throws IOException, InterruptedException {

        HttpResponse<String> req = Request.get("http://170.187.207.133:9000/connect");

        System.out.println(req.statusCode());

        /*UDPhelper test = new UDPhelper("170.187.207.133", Relay.UDPport);

        test.startRecieving(udpObject -> {

            System.out.println("data: ");
            System.out.println(Arrays.toString(udpObject.data));
            System.out.println(udpObject.uuid);

        });*/

        Player testPlayer = new Player("test", "test", 12, 1235, 567, 56);

        TCPHelper test = TCPHelper.startClient(tcpObject -> {

            System.out.println(tcpObject.uuid);

        }, "170.187.207.133", Relay.TCPport);

        System.out.println("out:");
        System.out.println(Arrays.toString(testPlayer.generateOut()));

        test.send(testPlayer.generateOut());

    }

}
