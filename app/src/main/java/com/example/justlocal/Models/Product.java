package com.example.justlocal.Models;

public class Product {
    private int productID;
    private int sellerID; // FK from Users table
    private String productName;
    private String productDescription;
    private String image; // Base64 or URL
    private double price;
    private int quantity;
    private String status; // e.g., "Approved", "Rejected", "Pending"
    private String approvedBy; // Can be user full name or user ID string

    // Constructors
    public Product() {
    }

    public Product(int productID, int sellerID, String productName, String productDescription, String image, double price, int quantity, String status, String approvedBy) {
        this.productID = productID;
        this.sellerID = sellerID;
        this.productName = productName;
        this.productDescription = productDescription;
        this.image = image;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
        this.approvedBy = approvedBy;
    }

    // Getters and Setters
    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public int getSellerID() {
        return sellerID;
    }

    public void setSellerID(int sellerID) {
        this.sellerID = sellerID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }
}
