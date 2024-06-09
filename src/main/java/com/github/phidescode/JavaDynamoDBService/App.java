package com.github.phidescode.JavaDynamoDBService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
/**
 * Lambda function entry point. You can change to use other pojo type or
 * implement a different RequestHandler.
 *
 * @see
 * <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda
 * Java Handler</a> for more information
 */
// public class App implements RequestHandler<Object, Object> {
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    // private final DynamoDbAsyncClient dynamoDbClient;
    private static HashMap<String, String> headers;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ORIGIN_URL = "http://localhost:3000";

    public App() {
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        // dynamoDbClient = DependencyFactory.dynamoDbClient();
        // Consider invoking a simple api here to pre-warm up the application, eg: dynamodb#listTables

        headers = new HashMap<>();
        // headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", ORIGIN_URL);
        headers.put("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        // LambdaLogger logger = context.getLogger();
        Logger.setLogger(context.getLogger());

        String httpMethod = request.getHttpMethod();
        Logger.log("Processing " + httpMethod + " request");

        return switch (httpMethod) {
            case "GET" ->
                processGet(request);
            case "POST" ->
                processPost(request);
            case "PUT" ->
                processPut(request);
            case "DELETE" ->
                processDelete(request);
            case "OPTIONS" ->
                processOptions();
            default ->
                clientError(HttpStatus.METHOD_NOT_ALLOWED);
        };
    }

    private APIGatewayProxyResponseEvent clientError(HttpStatus httpStatus) {
        int statusCode = httpStatus.value();
        String message = HttpStatus.valueOf(statusCode).getReasonPhrase();
        return createResponse(statusCode, message);
    }

    private APIGatewayProxyResponseEvent processGet(APIGatewayProxyRequestEvent request) {
        String path = request.getPath();
        String message = String.format("hello world! responding to your GET at %s", path);
        return createResponse(200, message);
    }

    private APIGatewayProxyResponseEvent processPost(APIGatewayProxyRequestEvent request) {
        String path = request.getPath();
        String message = String.format("hello world! responding to your POST at %s", path);
        return createResponse(200, message);
    }

    private APIGatewayProxyResponseEvent processPut(APIGatewayProxyRequestEvent request) {
        String path = request.getPath();
        String message = String.format("hello world! responding to your PUT at %s", path);
        return createResponse(200, message);
    }

    private APIGatewayProxyResponseEvent processDelete(APIGatewayProxyRequestEvent request) {
        String path = request.getPath();
        String message = String.format("hello world! responding to your DELETE at %s", path);
        return createResponse(200, message);
    }

    private APIGatewayProxyResponseEvent processOptions() {
        headers.put("Access-Control-Allow-Methods", "OPTIONS, POST, GET, PUT, DELETE");

        int statusCode = HttpStatus.OK.value();
        String statusText = HttpStatus.valueOf(statusCode).getReasonPhrase();

        return createResponse(statusCode, statusText);
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String message) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(statusCode);

        try {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", statusCode);
            responseBody.put("message", message);

            String responseBodyString = objectMapper.writeValueAsString(responseBody);
            response.setBody(responseBodyString);
        } catch (JsonProcessingException e) {
            response.setStatusCode(500);
            response.setBody("{\"error\":\"Internal Server Error\"}");
        }

        return response;
    }
}
