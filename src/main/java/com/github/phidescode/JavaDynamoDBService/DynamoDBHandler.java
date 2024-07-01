package com.github.phidescode.JavaDynamoDBService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

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
            entities.add(getEntityFromItem(item));
        }

        return entities;
    }

    public Entity getEntity(String id) throws InterruptedException, ExecutionException, NoSuchElementException {
        HashMap<String, AttributeValue> itemKey = new HashMap<>();
        itemKey.put("id", AttributeValue.builder()
                .s(id)
                .build());

        GetItemRequest getItemRequest = GetItemRequest.builder()
                .key(itemKey)
                .tableName(TABLE_NAME)
                .build();

        CompletableFuture<GetItemResponse> getItemResponseFuture = dynamoDbClient.getItem(getItemRequest);
        GetItemResponse getItemResponse = getItemResponseFuture.get();

        Map<String, AttributeValue> item = getItemResponse.item();

        if (item.isEmpty()) {
            throw new NoSuchElementException("Item not found with ID: " + id);
        }

        return getEntityFromItem(item);
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

    public void updateEntity(String id, BaseEntity entity) throws InterruptedException, ExecutionException, NoSuchElementException {
        getEntity(id);

        HashMap<String, AttributeValue> itemKey = new HashMap<>();
        itemKey.put("id", AttributeValue.builder()
                .s(id)
                .build());

        HashMap<String, AttributeValueUpdate> updatedValues = getUpdatedValues(entity);
        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(itemKey)
                .attributeUpdates(updatedValues)
                .build();

        CompletableFuture<UpdateItemResponse> updateItemResponseFuture = dynamoDbClient.updateItem(updateItemRequest);
        updateItemResponseFuture.get();
    }

    public void deleteEntity(String id) throws InterruptedException, ExecutionException, NoSuchElementException {
        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put("id", AttributeValue.builder()
                .s(id)
                .build());

        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .tableName(TABLE_NAME)
                .returnValues("ALL_OLD")
                .key(keyToGet)
                .build();

        CompletableFuture<DeleteItemResponse> deleteItemResponseFuture = dynamoDbClient.deleteItem(deleteItemRequest);
        DeleteItemResponse deleteItemResponse = deleteItemResponseFuture.get();

        if (deleteItemResponse.attributes() == null || deleteItemResponse.attributes().isEmpty()) {
            throw new NoSuchElementException("Item not found with ID: " + id);
        }
    }

    private Entity getEntityFromItem(Map<String, AttributeValue> item) {
        String id = item.get("id").s();
        String description = item.get("description").s();
        int quantity = Integer.parseInt(item.get("quantity").n());

        return new Entity(id, new BaseEntity(description, quantity));
    }

    private HashMap<String, AttributeValueUpdate> getUpdatedValues(BaseEntity entity) {
        HashMap<String, AttributeValueUpdate> updatedValues = new HashMap<>();

        updatedValues.put("description", AttributeValueUpdate.builder()
                .value(AttributeValue.builder()
                        .s(entity.getDescription())
                        .build())
                .action(AttributeAction.PUT)
                .build());

        updatedValues.put("quantity", AttributeValueUpdate.builder()
                .value(AttributeValue.builder()
                        .n(entity.getQuantity() + "")
                        .build())
                .action(AttributeAction.PUT)
                .build());

        return updatedValues;
    }

    private HashMap<String, AttributeValue> getItemValues(Entity entity) {
        HashMap<String, AttributeValue> itemValues = new HashMap<>();

        itemValues.put("id", AttributeValue.builder()
                .s(entity.getId())
                .build());

        itemValues.put("description", AttributeValue.builder()
                .s(entity.getDescription())
                .build());

        itemValues.put("quantity", AttributeValue.builder()
                .n(entity.getQuantity() + "")
                .build());

        return itemValues;
    }
}
