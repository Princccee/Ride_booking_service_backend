package com.ridebooking.dto;

import lombok.Data;

@Data
public class DriverRegisterRequest {
    private String username;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String vehicleNumber;
    private String vehicleModel;
    private String licenseNumber;
}
