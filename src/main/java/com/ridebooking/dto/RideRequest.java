package com.ridebooking.dto;

import lombok.Data;

@Data
public class RideRequest {
    private  String pickupLocation;
    private String dropLocation;
    private Long userId;
}
