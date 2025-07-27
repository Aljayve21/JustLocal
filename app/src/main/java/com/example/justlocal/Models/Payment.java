package com.example.justlocal.Models;

public class Payment {
    private String paymentID;
    private String orderID;
    private String method;
    private String message;
    private String status;
    private String paidAt;
    private String reference;

    public Payment() {}

    public Payment(String paymentID, String orderID, String method, String message,
                   String status, String paidAt, String reference) {
        this.paymentID = paymentID;
        this.orderID = orderID;
        this.method = method;
        this.message = message;
        this.status = status;
        this.paidAt = paidAt;
        this.reference = reference;
    }

    // Getters and setters...

    public String getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
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

    public String getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(String paidAt) {
        this.paidAt = paidAt;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
