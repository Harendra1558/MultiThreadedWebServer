package com.project.MultiThreadedWebServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * RouteResolver is responsible for mapping HTTP requests (GET and POST) to their respective handler methods
 * in controller classes. It registers routes based on annotations (@GetMapping, @PostMapping) and invokes the
 * appropriate method when a matching request is received.
 */
public class RouteResolver {


    // Create a logger instance for logging
    private static final Logger logger = LoggerFactory.getLogger(RouteResolver.class);

    // Map to store GET request routes with their corresponding handler methods
    private final Map<String, BiConsumer<BufferedReader, PrintWriter>> getRoutes = new ConcurrentHashMap<>();
    
    // Map to store POST request routes with their corresponding handler methods
    private final Map<String, BiConsumer<BufferedReader, PrintWriter>> postRoutes = new ConcurrentHashMap<>();

    /**
     * Constructor that initializes the RouteResolver and registers routes by scanning the controller class.
     */
    public RouteResolver() {
        // Register routes by scanning controller methods
        registerRoutes(new Controller());
    }

    /**
     * Registers the routes by scanning methods in the given controller class for GetMapping and PostMapping annotations.
     * The methods are then added to the appropriate route map (GET or POST).
     *
     * @param controller the controller instance to scan for route methods
     */
    private void registerRoutes(Controller controller) {
        // Iterate through all declared methods in the controller class
        for (Method method : controller.getClass().getDeclaredMethods()) {
            // Check if the method is annotated with @GetMapping
            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping annotation = method.getAnnotation(GetMapping.class);
                // Register the route with the URI from the annotation and the method as the handler
                getRoutes.put(annotation.value(), (reader, writer) -> invokeMethod(controller, method, reader, writer));
            } 
            // Check if the method is annotated with @PostMapping
            else if (method.isAnnotationPresent(PostMapping.class)) {
                PostMapping annotation = method.getAnnotation(PostMapping.class);
                // Register the route with the URI from the annotation and the method as the handler
                postRoutes.put(annotation.value(), (reader, writer) -> invokeMethod(controller, method, reader, writer));
            }
        }
    }

    /**
     * Invokes the appropriate method on the controller based on the method signature.
     * If the method has one parameter (PrintWriter), it will only pass the writer.
     * If the method has two parameters (BufferedReader, PrintWriter), both will be passed.
     *
     * @param controller the controller instance
     * @param method the method to invoke
     * @param reader the BufferedReader for reading the request
     * @param writer the PrintWriter for writing the response
     */
    private void invokeMethod(Controller controller, Method method, BufferedReader reader, PrintWriter writer) {
        try {
            // Check the number and types of parameters of the method to decide how to invoke it
            if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == PrintWriter.class) {
                // If the method takes a PrintWriter parameter, invoke it with the writer
                method.invoke(controller, writer);
            } else if (method.getParameterCount() == 2 && method.getParameterTypes()[0] == BufferedReader.class) {
                // If the method takes both a BufferedReader and PrintWriter parameter, invoke it with both
                method.invoke(controller, reader, writer);
            }
        } catch (Exception e) {
            // Print any exception that occurs during method invocation
            logger.error("Error in invokeMethod", e);

        }
    }

    /**
     * Resolves a request by matching the HTTP method (GET or POST) and URI to the appropriate route handler.
     * It then invokes the handler method with the provided reader and writer objects.
     *
     * @param method the HTTP method (GET or POST)
     * @param uri the URI of the request
     * @param reader the BufferedReader for reading the request
     * @param writer the PrintWriter for writing the response
     * @return true if a route was found and invoked, false otherwise
     */
    public boolean resolve(String method, String uri, BufferedReader reader, PrintWriter writer) {
        try {
            // Declare a handler variable to hold the method handler
            BiConsumer<BufferedReader, PrintWriter> handler = null;

            // Check for GET method and get the handler from the getRoutes map
            if ("GET".equalsIgnoreCase(method)) {
                handler = getRoutes.get(uri);
            }
            // Check for POST method and get the handler from the postRoutes map
            else if ("POST".equalsIgnoreCase(method)) {
                handler = postRoutes.get(uri);
            }

            // If a handler was found, invoke it
            if (handler != null) {
                handler.accept(reader, writer);
                return true;
            } else {
                // Return false if no handler was found for the URI
                return false;
            }
        } catch (Exception e) {
            // Print any exception that occurs during the request resolution process
            logger.error("Error in Routeing resolve method", e);
            return false;
        }
    }
}
