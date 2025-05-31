package com.ridebooking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Driver extends base{
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String vehicleNumber;

    @Column(nullable = false)
    private String vehicleModel;

    @Column(nullable = false, unique = true)
    private String licenceNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private driverStatus status; // 'AVAILABLE', 'ON_RIDE', etc.
}
