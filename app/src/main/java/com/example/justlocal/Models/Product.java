package com.example.justlocal.Models;

import java.util.List;

public class Product {
    private String productID;
    private String sellerID;
    private String productName;
    private String productDescription;
    private String image;
    private String price;
    private String quantity;
    private String status;
    private List<Double> embedding;  // ✅ embedding field
    private String approvedBy;
    private boolean isFavorited;
    private String firebaseKey;

    // Default constructor required for Firebase
    public Product() {}

    public Product(String productID, String sellerID, String productName, String productDescription,
                   String image, String price, String quantity, String status, String approvedBy,
                   List<Double> embedding) {
        this.productID = productID;
        this.sellerID = sellerID;
        this.productName = productName;
        this.productDescription = productDescription;
        this.image = image;
        this.price = price;
        this.quantity = quantity;
        this.status = status;
        this.approvedBy = approvedBy;
        this.embedding = embedding;
    }

    // ✅ Getters and Setters

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getSellerID() {
        return sellerID;
    }

    public void setSellerID(String sellerID) {
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
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

    // ✅ Embedding getter & setter
    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }
}
