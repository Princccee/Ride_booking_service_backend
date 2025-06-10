package com.ridebooking.dto;

import lombok.Data;

@Data
public class DriverLocationUpdateRequest {
    private Long driverId;
    private Double latitude;
    private Double longitude;
}
