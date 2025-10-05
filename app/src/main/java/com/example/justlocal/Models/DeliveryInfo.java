package com.example.justlocal.Models;

public class DeliveryInfo {
    private String deliveryID;
    private String orderID;
    private String carrier;
    private String trackingNo;
    private String status;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    private String timestamp;
    private String dateTime;

    public DeliveryInfo() {}



    public DeliveryInfo(String deliveryID, String orderID, String carrier,
                        String trackingNo, String status, String dateTime, String timestamp) {
        this.deliveryID = deliveryID;
        this.orderID = orderID;
        this.carrier = carrier;
        this.trackingNo = trackingNo;
        this.status = status;
        this.dateTime = dateTime;
        this.timestamp = timestamp;
    }

    // Getters and setters...

    public String getDeliveryID() {
        return deliveryID;
    }

    public void setDeliveryID(String deliveryID) {
        this.deliveryID = deliveryID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}

