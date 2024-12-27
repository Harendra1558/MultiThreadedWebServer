package com.project.MultiThreadedWebServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class Controller {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Create a logger instance for logging
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @PostMapping("/api/data")
    public static void handleApiData(BufferedReader reader, PrintWriter writer) {
        try {
            // Use utility to prepare the request body (ensure content type and read data)
            String requestBody = RequestProcessor.prepareRequest(reader, writer, "application/json");
            if (requestBody == null) return; // Exit if the request body is invalid or empty

            // Create a response containing the received data
            String jsonResponse = objectMapper.writeValueAsString(Map.of("status", "success", "receivedData", requestBody));
            WebServer.sendResponse(writer, 200, "OK", "application/json", jsonResponse);
        } catch (IOException e) {
            // Handle any I/O errors and respond with a 400 Bad Request error
            logger.error("Error processing /api/data request", e);
            WebServer.sendErrorResponse(writer, 400, "Bad Request", "application/json");
        }
    }


    @GetMapping("/")
    public static void showHome(PrintWriter writer) {
        // Create a simple HTML response for the home page
        String response = "<html><body><h1>Welcome to the Home Page</h1></body></html>";
        WebServer.sendResponse(writer, 200, "OK", "text/html", response);
    }


    @PostMapping("/test")
    public static void test(BufferedReader reader, PrintWriter writer) {
        try {
            // Use utility to prepare the request body (ensure content type and read data)
            String requestBody = RequestProcessor.prepareRequest(reader, writer, "application/json");
            if (requestBody == null) return; // Exit if the request body is invalid or empty

            // Process the JSON content
            handleJsonContent(requestBody, writer);
        } catch (IOException e) {
            // Handle any I/O errors and respond with a 400 Bad Request error
            logger.error("Error processing /test request", e);
            WebServer.sendErrorResponse(writer, 400, "Bad Request", "application/json");
        }
    }


    @PostMapping("/text")
    public static void text(BufferedReader reader, PrintWriter writer) {
        try {
            // Use utility to prepare the request body (ensure content type and read data)
            String requestBody = RequestProcessor.prepareRequest(reader, writer, "text/plain");
            if (requestBody == null) return; // Exit if the request body is invalid or empty

            // Send a response with the received plain text data
            WebServer.sendResponse(writer, 200, "OK", "text/plain", "Status : success -> " + requestBody);

        } catch (IOException e) {
            // Handle any I/O errors and respond with a 400 Bad Request error
            WebServer.sendErrorResponse(writer, 400, "Bad Request", "application/json");
        }
    }



    private static void handleJsonContent(String requestBody, PrintWriter writer) {
        try {
            // Parse the JSON content into a Map
            Map<String, Object> requestData = objectMapper.readValue(requestBody, Map.class);

            // Create a JSON response with the received data
            String jsonResponse = objectMapper.writeValueAsString(Map.of("status", "success", "receivedData", requestData));
            WebServer.sendResponse(writer, 200, "OK", "application/json", jsonResponse);
        } catch (IOException e) {
            // Handle invalid JSON format and respond with a 400 Bad Request error
            logger.error("Error in handelJsonContent", e);
            WebServer.sendErrorResponse(writer, 400, "Invalid JSON Format", "application/json");
        }
    }

}


