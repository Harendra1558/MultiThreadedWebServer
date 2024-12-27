package com.project.MultiThreadedWebServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    private static final int PORT = 8080; // The server port to connect to
    private static final String HOST = "localhost"; // The server host (localhost in this case)

    // Logger to log messages for various actions within the Client class
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    /**
     * The main method that starts the client and creates a new thread for each connection.
     *
     * @param args Command line arguments (not used in this example)
     */
    public static void main(String[] args) {
        Client client = new Client();

        // Create and start a new thread to handle client-server communication
        for (int i = 0; i < 1000; i++) {
            try {
                Thread thread = new Thread(client.getRunnable());
                thread.start();
                logger.info("Started thread for client connection: {}", i);
            } catch (Exception ex) {
                logger.error("Error starting thread: ", ex);
            }
        }
    }

    /**
     * Creates and returns a Runnable that handles the client-server communication.
     * This method is used to create separate threads for each client connection.
     *
     * @return A Runnable that can be executed in a separate thread.
     */
    public Runnable getRunnable() {
        return () -> {
            try {
                // Resolve the server address
                InetAddress serverAddress = InetAddress.getByName(HOST);

                // Establish the socket connection to the server
                try (Socket socket = new Socket(serverAddress, PORT);
                     PrintWriter serverOutput = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader serverInput = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    // Create the client message
                    String clientMessage = "Hello from Client " + socket.getLocalSocketAddress();
                    serverOutput.println(clientMessage); // Send message to the server
                    logger.info("Sent to server: {}", clientMessage);

                    // Read the server's response
                    String serverResponse = serverInput.readLine();
                    logger.info("Response from Server: {}", serverResponse);

                } catch (IOException e) {
                    // Log any issues during communication with the server
                    logger.error("IOException occurred while communicating with server: ", e);
                }

            } catch (IOException e) {
                // Log issues with establishing the socket connection
                logger.error("IOException occurred while establishing socket connection: ", e);
            }
        };
    }
}
