package com.github.phidescode.JavaDynamoDBService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

public class DynamoDBHandler {

    private static final String TABLE_NAME = "AppnameBeans";

    private final DynamoDbAsyncClient dynamoDbClient;

    public DynamoDBHandler() {
        dynamoDbClient = DependencyFactory.dynamoDbClient();
    }

    public void closeDbClient() {
        dynamoDbClient.close();
    }

    public List<Entity> listEntities() throws InterruptedException, ExecutionException {

        List<Entity> entities = new ArrayList<>();

        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

        CompletableFuture<ScanResponse> scanResponseFuture = dynamoDbClient.scan(scanRequest);
        ScanResponse scanResponse = scanResponseFuture.get();

        List<Map<String, AttributeValue>> items = scanResponse.items();

        for (Map<String, AttributeValue> item : items) {
            String id = item.get("id").s();
            String description = item.get("description").s();
            int quantity = Integer.parseInt(item.get("quantity").n());

            Entity entity = new Entity(id, description, quantity);
            entities.add(entity);
        }

        return entities;
    }

    public Entity putEntity(BaseEntity newEntity) throws InterruptedException, ExecutionException {

        if (newEntity instanceof BaseEntity) {
            Entity entity = new Entity(newEntity);

            PutItemRequest newItemRequest = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(getItemValues(entity))
                    .build();

            CompletableFuture<PutItemResponse> putItemResponseFuture = dynamoDbClient.putItem(newItemRequest);
            putItemResponseFuture.get();

            return entity;
        } else {
            throw new ClassCastException("Invalid data");
        }
    }

    public void deleteEntity(String id) throws InterruptedException, ExecutionException, NoSuchElementException {

        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put("id", AttributeValue.builder()
                .s(id)
                .build());

        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(keyToGet)
                .build();

        CompletableFuture<DeleteItemResponse> deleteItemResponseFuture = dynamoDbClient.deleteItem(deleteItemRequest);

        DeleteItemResponse deleteItemResponse = deleteItemResponseFuture.get();

        if (deleteItemResponse.attributes() == null || deleteItemResponse.attributes().isEmpty()) {
            throw new NoSuchElementException("Item not found with ID: " + id);
        }
    }

    private HashMap<String, AttributeValue> getItemValues(Entity entity) {
        HashMap<String, AttributeValue> itemValues = new HashMap<>();

        itemValues.put("id", AttributeValue.builder().s(entity.getId()).build());
        itemValues.put("description", AttributeValue.builder().s(entity.getDescription()).build());
        itemValues.put("quantity", AttributeValue.builder().n(entity.getQuantity() + "").build());

        return itemValues;
    }
}
