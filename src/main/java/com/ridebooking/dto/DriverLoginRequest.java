package com.ridebooking.dto;

import lombok.Data;

@Data
public class DriverLoginRequest {
    private String username;
    private String password;
}
