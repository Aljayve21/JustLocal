package com.example.justlocal.Models;

public class Message {
    private String messageID;
    private String complaintID;
    private String senderID;
    private String content;
    private long timestamp;

    public Message() {
        // Required for Firebase
    }

    public Message(String messageID, String complaintID, String senderID, String content, long timestamp) {
        this.messageID = messageID;
        this.complaintID = complaintID;
        this.senderID = senderID;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getComplaintID() {
        return complaintID;
    }

    public void setComplaintID(String complaintID) {
        this.complaintID = complaintID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
