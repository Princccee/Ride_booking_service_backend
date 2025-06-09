package com.ridebooking.service;

import com.ridebooking.dto.RideRequest;
import com.ridebooking.model.*;
import com.ridebooking.repository.DriverRepository;
import com.ridebooking.repository.RideRepository;
import com.ridebooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class RideService {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private UserRepository userRepository;

    public Ride createRide(RideRequest request) {
        // Get user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find first available driver (for now, no geo logic)
        Driver driver = driverRepository.findAll().stream()
                .filter(d -> d.getStatus() == driverStatus.AVAILABLE)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No available drivers"));

        // Update driver status
        driver.setStatus(driverStatus.ON_RIDE);
        driverRepository.save(driver);

        // Create ride
        Ride ride = Ride.builder()
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropLocation())
                .rider(user)
                .driver(driver)
                .status(rideStatus.REQUESTED)
                .build();

        return rideRepository.save(ride);
    }

    public void acceptRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() != rideStatus.REQUESTED) {
            throw new RuntimeException("Ride is not in a REQUESTED state");
        }

        ride.setStatus(rideStatus.ACCEPTED); // update the ride status
        rideRepository.save(ride);
    }

    public void startRide(Long rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if(ride.getStatus() != rideStatus.ACCEPTED){
            throw new RuntimeException("Ride is not in a ACCEPTED state");
        }

        ride.setStatus(rideStatus.STARTED); // update the ride status
        ride.setStartTime(LocalDateTime.now());
        rideRepository.save(ride);
    }

    public Ride completeRide(Long rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(()-> new RuntimeException("Ride doesn't exist"));

        if(ride.getStatus() != rideStatus.STARTED){
            throw new RuntimeException("Ride is not in STARTED state");
        }

        // Calculate the fare based on the distance and time taken in the ride:
        double fare = 100.0;

        //Update the fields of a ride:
        ride.setDropoffLocation(String.valueOf(rideStatus.COMPLETED));
        ride.setFare(fare);
        ride.setCompletionTime(LocalDateTime.now());

        //mark the driver as available again:
        Driver driver = ride.getDriver();
        driver.setStatus(driverStatus.AVAILABLE);
        driverRepository.save(driver);

        return rideRepository.save(ride);
    }

}
