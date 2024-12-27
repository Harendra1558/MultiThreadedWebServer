package com.project.MultiThreadedWebServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT = 8010;
    private static final String DOCUMENT_ROOT = "webroot"; // Directory for static files

    public static void main(String[] args) {
        Server server = new Server();

        // Create a thread pool for managing client connections
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            serverSocket.setSoTimeout(900000); // Timeout for server socket
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    threadPool.execute(() -> server.handleClientRequest(clientSocket));
                } catch (SocketTimeoutException e) {
                    System.out.println("Server timed out waiting for connections.");
                    break;
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            threadPool.shutdown();
            System.out.println("Server shutting down...");
        }
    }


    private void handleClientRequest(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestLine = reader.readLine(); // Read the request line (GET, POST, etc.)
            if (requestLine == null) return;
            System.out.println("Request: " + requestLine);

            // Parse the request method and URI
            String[] requestParts = requestLine.split(" ");
            String method = requestParts[0];
            String uri = requestParts[1];

            // Handle GET request
            if ("GET".equals(method)) {
                if (uri.equals("/")) {
                    sendHtmlResponse(writer, "Welcome to the Server", "Hello, this is a basic server!");
                } else {
                    // Serve static files from the DOCUMENT_ROOT directory
                    File file = new File(DOCUMENT_ROOT, uri);
                    if (file.exists() && !file.isDirectory()) {
                        sendFileResponse(writer, file);
                    } else {
                        sendErrorResponse(writer, 404, "Not Found");
                    }
                }
            } else if ("POST".equals(method)) {
                handlePostRequest(reader, writer);
            } else {
                sendErrorResponse(writer, 405, "Method Not Allowed");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendHtmlResponse(PrintWriter writer, String title, String message) {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n" +
                "<html><head><title>" + title + "</title></head>" +
                "<body><h1>" + message + "</h1></body></html>";
        writer.write(response);
    }

    private void sendFileResponse(PrintWriter writer, File file) throws IOException {
        // Read file and send as response
        String fileContent = new String(Files.readAllBytes(file.toPath()));
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n" +
                fileContent;
        writer.write(response);
    }

    private void sendErrorResponse(PrintWriter writer, int statusCode, String statusMessage) {
        String response = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n\r\n" +
                "<html><head><title>" + statusMessage + "</title></head>" +
                "<body><h1>" + statusMessage + "</h1></body></html>";
        writer.write(response);
    }

    private void handlePostRequest(BufferedReader reader, PrintWriter writer) throws IOException {
        // Read the body of the POST request
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            requestBody.append(line).append("\n");
        }

        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "Connection: close\r\n\r\n" +
                "Received POST data:\n" +
                requestBody;
        writer.write(response);
    }

    private void sendResponse(PrintWriter writer, int statusCode, String statusMessage, String contentType, String body) {
        String headers = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Connection: close\r\n" +
                "Content-Length: " + body.length() + "\r\n\r\n"; // Add Content-Length dynamically

        writer.write(headers);
        writer.write(body);
    }
}
