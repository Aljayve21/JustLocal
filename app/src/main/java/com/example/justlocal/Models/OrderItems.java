package com.example.justlocal.Models;

public class OrderItems {
    private String itemID;
    private String orderID;
    private String productID;
    private String priceAtOrder;
    private int quantity;

    public OrderItems() {} // Required for Firebase

    public OrderItems(String itemID, String orderID, String productID, String priceAtOrder, int quantity) {
        this.itemID = itemID;
        this.orderID = orderID;
        this.productID = productID;
        this.priceAtOrder = priceAtOrder;
        this.quantity = quantity;
    }

    // Getters and setters
    public String getItemID() { return itemID; }
    public void setItemID(String itemID) { this.itemID = itemID; }

    public String getOrderID() { return orderID; }
    public void setOrderID(String orderID) { this.orderID = orderID; }

    public String getProductID() { return productID; }
    public void setProductID(String productID) { this.productID = productID; }

    public String getPriceAtOrder() { return priceAtOrder; }
    public void setPriceAtOrder(String priceAtOrder) { this.priceAtOrder = priceAtOrder; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

