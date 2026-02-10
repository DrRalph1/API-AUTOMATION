package com.usg.apiAutomation.dtos.userManagement;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BaseUserRequestDTO {

    @JsonProperty("entrySource")
    // @NotBlank(message = "Entry Source cannot be blank")
    private String entrySource;

    @JsonProperty("deviceIp")
    // @NotBlank(message = "Device IP cannot be blank")
    private String deviceIp;

    @JsonProperty("channel")
    // @NotBlank(message = "Channel cannot be blank")
    private String channel;

    @JsonProperty("authToken")
    // @NotBlank(message = "Auth token cannot be blank")
    private String authToken;

    @JsonProperty("userName")
    // @NotBlank(message = "Username cannot be blank")
    private String userName;

    @JsonProperty("deviceId")
    // @NotBlank(message = "Device ID cannot be blank")
    private String deviceId;

    @JsonProperty("deviceName")
    // @NotBlank(message = "Device Name cannot be blank")
    private String deviceName;

    @JsonProperty("country")
    // @NotBlank(message = "Country cannot be blank")
    private String country;

    @JsonProperty("brand")
    // @NotBlank(message = "Brand cannot be blank")
    private String brand;

    @JsonProperty("manufacturer")
    // @NotBlank(message = "Manufacturer cannot be blank")
    private String manufacturer;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

    // No-args constructor
    public BaseUserRequestDTO() {}

    // All-args constructor
    public BaseUserRequestDTO(String entrySource, String deviceIp, String channel,
                              String authToken, String userName, String deviceId,
                              String deviceName, String country, String brand,
                              String manufacturer, String phoneNumber) {
        this.entrySource = entrySource;
        this.deviceIp = deviceIp;
        this.channel = channel;
        this.authToken = authToken;
        this.userName = userName;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.country = country;
        this.brand = brand;
        this.manufacturer = manufacturer;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public String getEntrySource() {
        return entrySource;
    }

    public void setEntrySource(String entrySource) {
        this.entrySource = entrySource;
    }

    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // toString
    @Override
    public String toString() {
        return "BaseUserRequestDTO{" +
                "entrySource='" + entrySource + '\'' +
                ", deviceIp='" + deviceIp + '\'' +
                ", channel='" + channel + '\'' +
                ", authToken='" + authToken + '\'' +
                ", userName='" + userName + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", country='" + country + '\'' +
                ", brand='" + brand + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
