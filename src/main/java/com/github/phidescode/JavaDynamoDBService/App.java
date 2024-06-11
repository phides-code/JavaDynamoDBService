package com.github.phidescode.JavaDynamoDBService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.http.HttpStatus;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Lambda function entry point. You can change to use other pojo type or
 * implement a different RequestHandler.
 *
 * @see
 * <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda
 * Java Handler</a> for more information
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static HashMap<String, String> headers;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String ORIGIN_URL = "http://localhost:3000";

    public App() {
        headers = new HashMap<>();
        headers.put("Access-Control-Allow-Origin", ORIGIN_URL);
        headers.put("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        Logger.setLogger(context.getLogger());

        String httpMethod = request.getHttpMethod();
        Logger.log("Processing " + httpMethod + " request");

        return switch (httpMethod) {
            case "GET" ->
                processGet();
            // case "POST" ->
            //     processPost(request);
            // case "PUT" ->
            //     processPut(request);
            // case "DELETE" ->
            //     processDelete(request);
            case "OPTIONS" ->
                processOptions();
            default ->
                returnError(HttpStatus.METHOD_NOT_ALLOWED);
        };
    }

    private APIGatewayProxyResponseEvent returnError(HttpStatus httpStatus) {
        Logger.log("Running returnError");
        String errorMessage = httpStatus.getReasonPhrase();
        ResponseStructure responseContent = new ResponseStructure(null, errorMessage);

        return createResponse(httpStatus, responseContent);
    }

    private APIGatewayProxyResponseEvent processGet() {
        DynamoDBHandler dbHandler = new DynamoDBHandler();

        try {
            List<Entity> entities = dbHandler.listEntities();

            ResponseStructure responseContent = new ResponseStructure(entities, null);

            return createResponse(HttpStatus.OK, responseContent);
        } catch (InterruptedException | ExecutionException e) {
            Logger.logError("Error during DynamoDB scan: ", e);
            return returnError(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private APIGatewayProxyResponseEvent processOptions() {
        headers.put("Access-Control-Allow-Methods", "OPTIONS, POST, GET, PUT, DELETE");
        ResponseStructure responseContent = new ResponseStructure(null, null);

        return createResponse(HttpStatus.OK, responseContent);
    }

    private APIGatewayProxyResponseEvent createResponse(HttpStatus httpStatus, ResponseStructure responseContent) {

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        response.setIsBase64Encoded(false);
        response.setStatusCode(httpStatus.value());

        Map<String, Object> responseBody = new HashMap<>();

        try {
            Object responseData = responseContent.getData();
            String responseErrorMessage = responseContent.getErrorMessage();

            if (responseData != null) {
                responseBody.put("data", responseData);
                responseBody.put("errorMessage", null);
            } else {
                responseBody.put("data", null);
                responseBody.put("errorMessage", responseErrorMessage);
            }

            String responseBodyString = objectMapper.writeValueAsString(responseBody);
            response.setBody(responseBodyString);
        } catch (JsonProcessingException e) {
            Logger.logError("createResponse caught error: ", e);
            response.setStatusCode(500);

            response.setBody("{\"data\": null, \"errorMessage\": \"Internal Server Error\"}");
        }

        return response;
    }

    // private APIGatewayProxyResponseEvent processPost(APIGatewayProxyRequestEvent request) {
    //     String path = request.getPath();
    //     String message = String.format("hello world! responding to your POST at %s", path);
    //     return createResponse(200, message);
    // }
    // private APIGatewayProxyResponseEvent processPut(APIGatewayProxyRequestEvent request) {
    //     String path = request.getPath();
    //     String message = String.format("hello world! responding to your PUT at %s", path);
    //     return createResponse(200, message);
    // }
    // private APIGatewayProxyResponseEvent processDelete(APIGatewayProxyRequestEvent request) {
    //     String path = request.getPath();
    //     String message = String.format("hello world! responding to your DELETE at %s", path);
    //     return createResponse(200, message);
    // }
}
