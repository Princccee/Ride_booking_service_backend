package com.ridebooking.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rides")
public class Ride extends base{

    //Many rides can belong to a single user
    @ManyToOne
    @JoinColumn(name = "rider_id", nullable = false)
    private User rider;

    // many rides can also belong to a single driver
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(nullable = false)
    private String pickupLocation;

    @Column(nullable = false)
    private String dropoffLocation;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Enumerated(EnumType.STRING)
    private rideStatus status;
}
