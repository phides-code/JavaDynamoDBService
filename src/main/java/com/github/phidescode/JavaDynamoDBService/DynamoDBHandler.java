package com.github.phidescode.JavaDynamoDBService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

public class DynamoDBHandler {

    private final String TABLE_NAME = "beans";
    private final DynamoDbAsyncClient dynamoDbClient;

    public DynamoDBHandler() {
        dynamoDbClient = DependencyFactory.dynamoDbClient();
    }

    public List<Entity> listEntities() {

        List<Entity> entities = new ArrayList<>();

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        CompletableFuture<ScanResponse> scanResponseFuture = dynamoDbClient.scan(scanRequest);

        ScanResponse scanResponse;

        try {
            scanResponse = scanResponseFuture.get();
            List<Map<String, AttributeValue>> items = scanResponse.items();

            for (Map<String, AttributeValue> item : items) {
                String id = item.get("id").s();
                String description = item.get("description").s();
                int quantity = Integer.parseInt(item.get("quantity").n());

                Entity entity = new Entity(id, description, quantity);
                entities.add(entity);
            }
        } catch (InterruptedException | ExecutionException e) {
            Logger.logError("Error during DynamoDB scan", e);
        }

        return entities;
    }
}
