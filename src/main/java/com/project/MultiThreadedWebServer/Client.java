package com.project.MultiThreadedWebServer;

import java.io.*;
import java.net.*;

public class Client {

    private static final int PORT = 8010;
    private static final String HOST = "localhost";

    public static void main(String[] args) {
        Client client = new Client();

        for (int i = 0; i < 1; i++) {
            try {
                Thread thread = new Thread(client.getRunnable());
                thread.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public Runnable getRunnable() {
        return () -> {
            try {
                InetAddress serverAddress = InetAddress.getByName(HOST);

                try (Socket socket = new Socket(serverAddress, PORT);
                     PrintWriter serverOutput = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                ) {
                    // Send a message to the server
                    String clientMessage = "Hello from Client " + socket.getLocalSocketAddress();
                    serverOutput.println(clientMessage);
                    System.out.println("Sent to server: " + clientMessage);

                    // Read the response from the server
                    String serverResponse = serverInput.readLine();
                    System.out.println("Response from Server: " + serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Ensure the socket is closed
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
}
