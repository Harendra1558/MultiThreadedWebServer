package com.project.MultiThreadedWebServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {


    private static final Logger logger = LoggerFactory.getLogger(WebServer.class);

    /**
     * Port number on which the server listens for connections.
     */
    private static final int PORT = 8080;

    /**
     * Number of threads in the thread pool.
     */
    private static final int THREAD_POOL_SIZE = 10;

    /**
     * Main method to start the web server.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        WebServer server = new WebServer();
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        server.startServer(threadPool);
    }

    /**
     * Reads the body of the HTTP request from the client.
     *
     * @param reader  the BufferedReader to read the request body.
     * @param headers the parsed HTTP headers.
     * @return the request body as a String.
     * @throws IOException if an I/O error occurs.
     */
    public static String readRequestBody(BufferedReader reader, Map<String, String> headers) throws IOException {
        int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
        char[] body = new char[contentLength];
        int bytesRead = reader.read(body, 0, contentLength);

        if (bytesRead == contentLength) {
            return new String(body);
        } else {
            throw new RuntimeException("Incomplete Body");
        }
    }

    /**
     * Sends an HTTP error response to the client.
     *
     * @param writer         the PrintWriter to write the response.
     * @param statusCode     the HTTP status code.
     * @param statusMessage  the HTTP status message.
     * @param responseFormat the content type of the response (e.g., JSON or HTML).
     */
    public static void sendErrorResponse(PrintWriter writer, int statusCode, String statusMessage, String responseFormat) {
        // Determine response body based on format
        String body = switch (responseFormat.toLowerCase()) {
            case "application/json" -> "{\"error\": \"" + statusMessage + "\"}";
            case "text/html" -> "<html><head><title>" + statusMessage + "</title></head>" +
                    "<body><h1>" + statusMessage + "</h1></body></html>";
            default -> "Error: " + statusMessage;
        };

        sendResponse(writer, statusCode, statusMessage, responseFormat, body);
    }

    /**
     * Parses the HTTP headers from the client request.
     *
     * @param reader the BufferedReader to read the headers.
     * @return a Map containing header names and their values.
     * @throws IOException if an I/O error occurs.
     */
    public static Map<String, String> parseHeaders(BufferedReader reader) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;

        // Read headers line by line until an empty line is encountered
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int separatorIndex = line.indexOf(": ");
            if (separatorIndex != -1) {
                String key = line.substring(0, separatorIndex).trim();
                String value = line.substring(separatorIndex + 2).trim();
                headers.put(key, value);
            }
        }

        return headers;
    }

    /**
     * Sends an HTTP response to the client.
     *
     * @param writer        the PrintWriter to write the response.
     * @param statusCode    the HTTP status code.
     * @param statusMessage the HTTP status message.
     * @param contentType   the content type of the response (e.g., JSON, HTML).
     * @param body          the response body.
     */
    public static void sendResponse(PrintWriter writer, int statusCode, String statusMessage, String contentType, String body) {
        writer.printf("HTTP/1.1 %d %s\r\n", statusCode, statusMessage);
        writer.printf("Content-Type: %s\r\n", contentType);
        writer.printf("Content-Length: %d\r\n", body.length());
        writer.println("Connection: close\r\n");
        writer.print(body);
    }

    /**
     * Starts the web server and listens for client connections.
     *
     * @param threadPool the thread pool for handling client requests concurrently.
     */
    private void startServer(ExecutorService threadPool) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Web Server is running on port {}", PORT);
            while (true) {
                try {
                    // Accept new client connections
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New client connected: {}", clientSocket.getInetAddress());
                    serverSocket.setSoTimeout(900000); // Timeout for server socket

                    // Handle client request in a separate thread
                    threadPool.execute(() -> handleClientRequest(clientSocket));
                } catch (IOException ex) {
                    logger.error("Error Occurred in Accept new client connections", ex);
                    break;
                }
            }
        } catch (IOException ex) {
            logger.error("Error Occurred in starting server ", ex);
        }
    }

    /**
     * Handles an individual client request.
     *
     * @param clientSocket the client socket.
     */
    public void handleClientRequest(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Read the request line (e.g., "GET / HTTP/1.1")
            String requestLine = reader.readLine();
            if (requestLine == null) return;

            logger.info("Request: " + requestLine);

            // Parse request line into method and URI
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                sendErrorResponse(writer, 400, "Bad Request", "text/html");
                return;
            }

            String method = requestParts[0];
            String uri = requestParts[1];

            // Create route resolver instance
            RouteResolver routeResolver = new RouteResolver();

            // Resolve the route and handle the request
            boolean routeResolved = routeResolver.resolve(method, uri, reader, writer);

            // If no route found, send a 404 error
            if (!routeResolved) {
                sendErrorResponse(writer, 404, "Not Found", "text/html");
            }

        } catch (IOException e) {
            logger.error("Error in handel client request method ", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.error("Error in closing client socket ", e);
            }
        }
    }

    /**
     * Determines the response format based on the Accept header.
     *
     * @param acceptHeader the value of the Accept header.
     * @return the response format (e.g., JSON, HTML, or plain text).
     */
    private String determineResponseFormat(String acceptHeader) {
        if (acceptHeader.contains("application/json")) {
            return "application/json";
        } else if (acceptHeader.contains("text/html")) {
            return "text/html";
        } else if (acceptHeader.contains("text/plain")) {
            return "text/plain";
        } else {
            return "application/json";
        }
    }


}
