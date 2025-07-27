package com.example.justlocal.Models;

public class Address {
    private String addressID;
    private String userID;
    private String type; // e.g., Home, Office
    private String street;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private String contactNo;
    private boolean defaultAddress;

    public Address() {}

    public Address(String addressID, String userID, String type, String street,
                   String city, String province, String postalCode, String country,
                   String contactNo, boolean defaultAddress) {
        this.addressID = addressID;
        this.userID = userID;
        this.type = type;
        this.street = street;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.country = country;
        this.contactNo = contactNo;
        this.defaultAddress = defaultAddress;
    }

    public String getAddressID() {
        return addressID;
    }

    public void setAddressID(String addressID) {
        this.addressID = addressID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public boolean isDefaultAddress() { return defaultAddress; }
    public void setDefaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; }

    // Getters and setters...
}

