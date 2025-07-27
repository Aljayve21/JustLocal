package com.example.justlocal.Models;

public class Complaint {
    private String complaintID;
    private String customerID;
    private String orderID;
    private String productID;
    private String message;
    private String status;
    private String repliedBy;

    public Complaint() {}

    public Complaint(String complaintID, String customerID, String orderID, String productID,
                     String message, String status, String repliedBy) {
        this.complaintID = complaintID;
        this.customerID = customerID;
        this.orderID = orderID;
        this.productID = productID;
        this.message = message;
        this.status = status;
        this.repliedBy = repliedBy;
    }

    // Getters and setters...

    public String getComplaintID() {
        return complaintID;
    }

    public void setComplaintID(String complaintID) {
        this.complaintID = complaintID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRepliedBy() {
        return repliedBy;
    }

    public void setRepliedBy(String repliedBy) {
        this.repliedBy = repliedBy;
    }
}
