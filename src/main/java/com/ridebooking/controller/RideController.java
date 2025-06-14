package com.ridebooking.controller;

import com.ridebooking.dto.RideRatingRequest;
import com.ridebooking.dto.RideRequest;
import com.ridebooking.model.Ride;
import com.ridebooking.model.rideStatus;
import com.ridebooking.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/rides")
public class RideController {
    @Autowired
    private RideService rideService;

    @PostMapping("/book")
    public ResponseEntity<?> bookRide(@RequestBody RideRequest request) {
        Ride ride = rideService.createRide(request);
        return ResponseEntity.ok(ride);
    }

    @PostMapping("/{rideId}/accept")
    public ResponseEntity<String> acceptRide(@PathVariable Long rideId){
        rideService.acceptRide(rideId);
        return ResponseEntity.ok("Ride accepted successfully");
    }

    @PostMapping("/{rideId}/start")
    public ResponseEntity<String> startRide(@PathVariable("rideId") Long rideId){
        rideService.startRide(rideId);
        return ResponseEntity.ok("Ride started successfully");
    }

    @PostMapping("/{rideId}/complete")
    public ResponseEntity<String> completeRide(@PathVariable Long rideId,
                                               @RequestParam double distanceKm,
                                               @RequestParam double durationMinutes) {
        rideService.completeRide(rideId, distanceKm, durationMinutes);
        return ResponseEntity.ok("Ride completed and fare calculated.");
    }


    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<?> cancelRide(@PathVariable Long rideId){
        try{
            Ride cancelRide = rideService.cancelRide(rideId);
            return ResponseEntity.ok(cancelRide);
        }
        catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{rideId}/track")
    public ResponseEntity<?> trackRide(@PathVariable Long rideId){
        try{
            rideStatus rideStatus = rideService.getRideStatus(rideId);
            return ResponseEntity.ok(rideStatus);
        }
        catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/user/{userId}/rides")
    public ResponseEntity<List<Ride>> getUserRideHistory(@PathVariable Long userId){
        List<Ride> rides = rideService.getUserRideHistory(userId);
        return ResponseEntity.ok(rides);
    }

    @PostMapping("/driver/{driverId}/rides")
    public ResponseEntity<List<Ride>> getDriverRideHistory(@PathVariable Long driverId){
        List<Ride> rides = rideService.getDirverRideHistory(driverId);
        return ResponseEntity.ok(rides);
    }

    @PostMapping("/rate")
    public ResponseEntity<?> rateRide(@RequestBody RideRatingRequest request){
        rideService.rateRide(request);
        return ResponseEntity.ok("rating submitted successfully");
    }

    @GetMapping("/user/{userId}/current")
    public ResponseEntity<?> getCurrentRideForUser(@PathVariable Long userId) {
        return rideService.getCurrentRideForUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/driver/{driverId}/current")
    public ResponseEntity<?> getCurrentRideForDriver(@PathVariable Long driverId){
        return rideService.getCurrentRideForDriver(driverId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
