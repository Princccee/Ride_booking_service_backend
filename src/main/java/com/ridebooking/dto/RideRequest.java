package com.ridebooking.dto;

import lombok.Data;

@Data
public class RideRequest {
    private  String pickupLocation;
    private String dropLocation;
    private double pickupLattitude;
    private double pickupLongitude;
    private Long userId;
}
