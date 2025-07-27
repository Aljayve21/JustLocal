package com.example.justlocal.Models;

public class Wishlist {
    private String id;
    private String customerID;
    private String productID;
    private String status; // e.g., "Added", "Removed"

    public Wishlist() {
        // Required empty constructor
    }


    public Wishlist(String id, String customerID, String productID, String status) {
        this.id = id;
        this.customerID = customerID;
        this.productID = productID;
        this.status = status;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
