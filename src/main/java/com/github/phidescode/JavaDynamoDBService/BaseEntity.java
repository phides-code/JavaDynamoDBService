package com.github.phidescode.JavaDynamoDBService;

public class BaseEntity {

    protected String description;
    protected int quantity;

    // Jackson requires a default (no-argument) constructor to create an instance of BaseEntity during deserialization
    public BaseEntity() {
    }

    public BaseEntity(String description, int quantity) {
        this.description = description;
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
