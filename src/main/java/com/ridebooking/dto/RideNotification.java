package com.ridebooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RideNotification {
    private Long rideId;
    private String pickupLocation;
    private String dropoffLocation;
}
