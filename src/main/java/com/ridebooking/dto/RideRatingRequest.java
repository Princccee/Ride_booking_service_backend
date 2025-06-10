package com.ridebooking.dto;

import lombok.Data;

@Data
public class RideRatingRequest {
    private Long rideId;
    private Integer rating;
    private String feedback;
    private boolean isDriver;
}
