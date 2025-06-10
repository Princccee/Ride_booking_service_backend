package com.ridebooking.service;

import com.ridebooking.dto.RideRatingRequest;
import com.ridebooking.dto.RideRequest;
import com.ridebooking.model.*;
import com.ridebooking.repository.DriverRepository;
import com.ridebooking.repository.RideRepository;
import com.ridebooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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

    public Ride completeRide(Long rideId, double distance, double duration){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(()-> new RuntimeException("Ride doesn't exist"));

        if(ride.getStatus() != rideStatus.STARTED){
            throw new RuntimeException("Ride is not in STARTED state");
        }

        // Calculate the fare based on the distance and time taken in the ride:
        ride.setDistanceKm(distance);
        ride.setDurationMinutes(duration);

        //Update the fields of a ride:
        ride.setFare(calculateFare(distance, duration));
        ride.setStatus(rideStatus.COMPLETED);
        ride.setCompletionTime(LocalDateTime.now());

        //mark the driver as available again:
        Driver driver = ride.getDriver();
        driver.setStatus(driverStatus.AVAILABLE);
        driverRepository.save(driver);

        return rideRepository.save(ride);
    }


    public Ride cancelRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() == rideStatus.COMPLETED || ride.getStatus() == rideStatus.CANCELLED) {
            throw new RuntimeException("Ride is already completed or cancelled");
        }

        // Update the status of the ride to CANCELLED
        ride.setStatus(rideStatus.CANCELLED);

        // Set driver to AVAILABLE if assigned
        if (ride.getDriver() != null) {
            Driver driver = ride.getDriver();
            driver.setStatus(driverStatus.AVAILABLE);
            driverRepository.save(driver);
        }

       return rideRepository.save(ride);
    }

    public rideStatus getRideStatus(Long rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride doesn't exists"));

        return ride.getStatus();
    }

    public List<Ride> getUserRideHistory(Long userId){
        return rideRepository.findByRiderId(userId);
    }

    public List<Ride> getDirverRideHistory(Long driverId){
        return rideRepository.findByDriverId(driverId);
    }

    public void rateRide(RideRatingRequest request){
        Ride ride = rideRepository.findById(request.getRideId())
                .orElseThrow(()-> new RuntimeException("Ride not found"));

        if(ride.getStatus() != rideStatus.COMPLETED)
            throw  new RuntimeException("Can't rate a ride that is not in COMPLETED state");

        // rating by driver:
        if(request.isDriver()){
            ride.setDriverRating(request.getRating());
            ride.setDriverFeedback(request.getFeedback());
        }
        else{
            ride.setUserRating(request.getRating());
            ride.setUserFeedback(request.getFeedback());
        }

        rideRepository.save(ride);
    }

    public Optional<Ride> getCurrentRideForUser(Long userID){
        User user = userRepository.findById(userID)
                .orElseThrow(()-> new RuntimeException("User doesn't exists"));
        return rideRepository.findByRiderAndStatusIn(user, List.of(rideStatus.ACCEPTED, rideStatus.STARTED));
    }

    public Optional<Ride> getCurrentRideForDriver(Long driverId){
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(()-> new RuntimeException("Driver doesn't exist"));
        return rideRepository.findByDriverAndStatusIn(driver, List.of(rideStatus.ACCEPTED, rideStatus.STARTED));
    }

    private double calculateFare(double distanceKm, double durationMinutes){
        double baseFare = 30;
        double ratePerKm = 10;
        double ratePerMin = 2;

        return baseFare + (ratePerKm * distanceKm) + (ratePerMin * durationMinutes);
    }


}
