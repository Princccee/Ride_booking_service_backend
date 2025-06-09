package com.ridebooking.controller;

import com.ridebooking.dto.RideRequest;
import com.ridebooking.model.Ride;
import com.ridebooking.model.rideStatus;
import com.ridebooking.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> completeRide(@PathVariable Long rideId){
        try{
            Ride compleRide = rideService.completeRide(rideId);
            return ResponseEntity.ok(compleRide);
        }
        catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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


}
