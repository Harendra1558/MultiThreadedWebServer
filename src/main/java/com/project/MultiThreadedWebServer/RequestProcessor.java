package com.project.MultiThreadedWebServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class RequestProcessor {



    /**
     * Prepares the HTTP request by reading the headers, validating the content type,
     * and reading the request body. If the content type is invalid or the body is empty,
     * appropriate error responses are sent.
     *
     * @param reader the BufferedReader used to read the request data
     * @param writer the PrintWriter used to write the response
     * @param expectedContentType the expected content type (e.g., "application/json")
     * @return the request body as a string, or null if there was an error
     * @throws IOException if an I/O error occurs during reading the request
     */
    public static String prepareRequest(BufferedReader reader, PrintWriter writer, String expectedContentType) throws IOException {
        // Parse the request headers from the BufferedReader
        Map<String, String> headers = WebServer.parseHeaders(reader);

        // Get the "Content-Type" header value, defaulting to "application/json" if not provided
        String contentType = headers.getOrDefault("Content-Type", "application/json").trim();

        // Validate that the Content-Type matches the expected value
        if (!expectedContentType.equalsIgnoreCase(contentType)) {
            // Send 415 Unsupported Media Type error response if the content type is invalid
            WebServer.sendErrorResponse(writer, 415, "Unsupported Media Type", "text/plain");
            return null;
        }

        // Read and sanitize the request body based on the headers
        String requestBody = WebServer.readRequestBody(reader, headers);

        // If the body is empty , send a 400 Bad Request error response
        if ( requestBody.isEmpty()) {
            WebServer.sendErrorResponse(writer, 400, "Empty Request Body", "application/json");
            return null;
        }

        // Return the sanitized request body
        return requestBody;
    }

}
