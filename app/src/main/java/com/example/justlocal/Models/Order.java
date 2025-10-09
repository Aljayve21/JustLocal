package com.example.justlocal.Models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Order {
    private String orderID;
    private String customerID;
    private String sellerID;
    private String addressID;
    private String paymentMethod;
    private String deliveryMethod;
    private String trackingNo;
    private String orderDate;
    private String status;
    private String totalAmount;
    private long orderTimestamp;

    public long getOrderTimestamp() {
        if (orderDate == null) return 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(orderDate);
            return date.getTime();
        } catch (Exception e) {
            return 0;
        }
    }


    public void setOrderTimestamp(long orderTimestamp) {
        this.orderTimestamp = orderTimestamp;
    }

    public Order() {
        // Required empty constructor for Firebase
    }

    public Order(String orderID, String customerID, String sellerID, String addressID,
                 String paymentMethod, String deliveryMethod, String trackingNo,
                 String orderDate, String status, String totalAmount, long orderTimestamp) {
        this.orderID = orderID;
        this.customerID = customerID;
        this.sellerID = sellerID;
        this.addressID = addressID;
        this.paymentMethod = paymentMethod;
        this.deliveryMethod = deliveryMethod;
        this.trackingNo = trackingNo;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.orderTimestamp = orderTimestamp;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getSellerID() {
        return sellerID;
    }

    public void setSellerID(String sellerID) {
        this.sellerID = sellerID;
    }

    public String getAddressID() {
        return addressID;
    }

    public void setAddressID(String addressID) {
        this.addressID = addressID;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public String getOrderDate() {
        return orderDate;
    }



    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }


    // Getters and setters...
}

