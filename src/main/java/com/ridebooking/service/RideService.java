package com.ridebooking.service;

import com.razorpay.Order;
import com.ridebooking.dto.RideRatingRequest;
import com.ridebooking.dto.RideRequest;
import com.ridebooking.model.*;
import com.ridebooking.repository.DriverRepository;
import com.ridebooking.repository.RideRepository;
import com.ridebooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RideService {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private FCMService fcmService;

    @Autowired
    private PaymentService paymentService;

    private static final double EARTH_RADIUS_KM = 6371;

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    // Using Haversine distance between pickup & drop to compute the fare
    private double calculateFare(double distanceKm, double durationMinutes){
        double baseFare = 30;
        double ratePerKm = 10;
        double ratePerMin = 2;

        return baseFare + (ratePerKm * distanceKm) + (ratePerMin * durationMinutes);
    }

    public double estimateFare(double pickupLat, double pickupLng, double dropLat, double dropLng) {
        double distanceKm = calculateDistance(pickupLat, pickupLng, dropLat, dropLng);

        // Estimated duration based on average speed (e.g., 40 km/h)
        double averageSpeedKmPerHr = 40.0;
        double estimatedDurationMin = (distanceKm / averageSpeedKmPerHr) * 60;

        return calculateFare(distanceKm, estimatedDurationMin);
    }

    private void notifyRideTakenToOtherDrivers(Long rideId, Long acceptedDriverId, List<Driver> nearbyDrivers) {
        for (Driver d : nearbyDrivers) {
            if (!d.getId().equals(acceptedDriverId) && d.getFcmToken() != null) {
                fcmService.sendNotification(
                        d.getFcmToken(),
                        "Ride Unavailable",
                        "Ride #" + rideId + " has already been accepted"
                );
            }
        }
    }

    // Function to create a new ride, notify the nearby drivers
    public Ride createRide(RideRequest request) {
        // Step 1: Fetch User
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        double pickupLat = request.getPickupLattitude();
        double pickupLng = request.getPickupLongitude();

        // proximity to search in for the drivers
        double radiusKm = 5.0;

        // Get all available nearby drivers in 5km radius
        List<Driver> nearbyDrivers = driverRepository.findAll().stream()
                .filter(driver -> driver.getStatus() == driverStatus.AVAILABLE)
                .filter(driver -> driver.getCurrentLatitude() != null && driver.getCurrentLongitude() != null)
                .filter(driver -> calculateDistance(pickupLat, pickupLng, driver.getCurrentLatitude(), driver.getCurrentLongitude()) <= radiusKm)
                .toList();

        if(nearbyDrivers.isEmpty()) throw new RuntimeException("No driver available now");

        //Notify the nearby drivers
        for (Driver d : nearbyDrivers) {
            if (d.getFcmToken() != null) {
                fcmService.sendNotification(
                        d.getFcmToken(),
                        "New Ride Request",
                        "Pickup: " + request.getPickupLocation() +
                                ", Drop: " + request.getDropLocation()
                );
            }
        }

        // Step 2: Create ride WITHOUT assigning driver
        Ride ride = Ride.builder()
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropLocation())
                .pickupLattitude(request.getPickupLattitude())
                .pickupLongitude(request.getPickupLongitude())
                .rider(user)
                .status(rideStatus.REQUESTED)
                .build();

        return rideRepository.save(ride);
    }

    // This function handles the logic of accepting a ride from the driver side, and when a ride is accepted notify the other drivers as well that ride has already been taken.
    public void acceptRide(Long rideId, Long driverId) {
        //Get the ride for which the request has been raised
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if(ride.getStatus() != rideStatus.REQUESTED && ride.getDriver() != null)
            throw new RuntimeException("Ride has already been accepted");

        // fetch the driver from DB who is going to accept the ride
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        if (driver.getStatus() != driverStatus.AVAILABLE)
            throw new RuntimeException("Driver is not available");

        // Assign driver to the ride
        ride.setDriver(driver);
        ride.setStatus(rideStatus.ACCEPTED);

        // Update driver status
        driver.setStatus(driverStatus.ON_RIDE);

        // Save both updates
        driverRepository.save(driver);
        rideRepository.save(ride);

        //fetch the ride pickup coordinates and notify the other drivers that ride is already taken
        double pickupLat = ride.getPickupLattitude();
        double pickupLng = ride.getPickupLattitude();

        // proximity to search in for the drivers
        double radiusKm = 5.0;

        List<Driver> nearbyDrivers = driverRepository.findAll().stream()
                .filter(drivers -> drivers.getStatus() == driverStatus.AVAILABLE)
                .filter(drivers -> drivers.getCurrentLatitude() != null && driver.getCurrentLongitude() != null)
                .filter(drivers -> calculateDistance(pickupLat, pickupLng, driver.getCurrentLatitude(), driver.getCurrentLongitude()) <= radiusKm)
                .toList();

        // Notify other drivers (WebSocket) that ride has been taken
        if(!nearbyDrivers.isEmpty())
            notifyRideTakenToOtherDrivers(rideId, driverId, nearbyDrivers);
    }

    public void startRide(Long rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if(ride.getStatus() != rideStatus.ACCEPTED)
            throw new RuntimeException("Ride is not in a ACCEPTED state");

        ride.setStatus(rideStatus.STARTED); // update the ride status
        ride.setStartTime(LocalDateTime.now());
        rideRepository.save(ride);
    }

//    public void completeRide(Long rideId, double distance, double duration){
//        Ride ride = rideRepository.findById(rideId)
//                .orElseThrow(()-> new RuntimeException("Ride doesn't exist"));
//
//        if(ride.getStatus() != rideStatus.STARTED){
//            throw new RuntimeException("Ride is not in STARTED state");
//        }
//
//        // Calculate the fare based on the distance and time taken in the ride:
//        ride.setDistanceKm(distance);
//        ride.setDurationMinutes(duration);
//
//        //Update the fields of a ride:
//        ride.setFare(calculateFare(distance, duration));
//        ride.setStatus(rideStatus.COMPLETED);
//        ride.setCompletionTime(LocalDateTime.now());
//
//        //mark the driver as available again:
//        Driver driver = ride.getDriver();
//        driver.setStatus(driverStatus.AVAILABLE);
//        driverRepository.save(driver);
//
//        rideRepository.save(ride);
//
//        // Generate payment order via Razorpay
//        Order order = paymentService.createOrder(ride.getFare(), "ride_" + rideId);
//
//        ride.setTransactionId(order.get("id")); // Razorpay order ID
//        ride.setPaymentStatus("PENDING"); // Until confirmed from frontend/webhook
//        rideRepository.save(ride);
//    }

    public void completeRide(Long rideId, double distance, double duration) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride doesn't exist"));

        if (ride.getStatus() != rideStatus.STARTED) {
            throw new RuntimeException("Ride is not in STARTED state");
        }

        // Set distance and duration
        ride.setDistanceKm(distance);
        ride.setDurationMinutes(duration);

        // Calculate fare
        double fare = calculateFare(distance, duration);
        ride.setFare(fare);

        // Set completion status & time
        ride.setStatus(rideStatus.COMPLETED);
        ride.setCompletionTime(LocalDateTime.now());

        // Mark driver as available again
        Driver driver = ride.getDriver();
        driver.setStatus(driverStatus.AVAILABLE);
        driverRepository.save(driver);

        // Generate Razorpay payment order
        try {
            Order razorpayOrder = paymentService.createOrder(fare, "ride_" + rideId);

            // Store transaction ID & mark payment as PENDING
            ride.setTransactionId(razorpayOrder.get("id"));
            ride.setPaymentStatus(paymentStatus.PENDING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay payment order: " + e.getMessage());
        }

        // Save ride
        rideRepository.save(ride);
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




}
