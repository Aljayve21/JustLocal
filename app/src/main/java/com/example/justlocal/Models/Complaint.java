package com.example.justlocal.Models;

public class Complaint {
    private String complaintID;
    private String customerID;
    private String orderID;
    private String productID;
    private String message;
    private String status;
    private String repliedBy;
    private String supportReply;

    private long dateCreated;
    private long lastUpdated;
    private long replyDate;

    public Complaint() {}

    public Complaint(String complaintID, String customerID, String orderID, String productID,
                     String message, String status, String repliedBy, String supportReply,
                     long dateCreated, long lastUpdated, long replyDate) {
        this.complaintID = complaintID;
        this.customerID = customerID;
        this.orderID = orderID;
        this.productID = productID;
        this.message = message;
        this.status = status;
        this.repliedBy = repliedBy;
        this.supportReply = supportReply;
        this.dateCreated = dateCreated;
        this.lastUpdated = lastUpdated;
        this.replyDate = replyDate;
    }

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

    public String getSupportReply() {
        return supportReply;
    }

    public void setSupportReply(String supportReply) {
        this.supportReply = supportReply;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public long getReplyDate() {
        return replyDate;
    }

    public void setReplyDate(long replyDate) {
        this.replyDate = replyDate;
    }
}