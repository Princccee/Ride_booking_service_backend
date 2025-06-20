package com.ridebooking.controller;

import com.ridebooking.dto.RideRatingRequest;
import com.ridebooking.dto.RideRequest;
import com.ridebooking.model.Ride;
import com.ridebooking.model.paymentStatus;
import com.ridebooking.model.rideStatus;
import com.ridebooking.repository.RideRepository;
import com.ridebooking.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("api/rides")
public class RideController {
    @Autowired
    private RideService rideService;

    private RideRepository rideRepository;

    @PostMapping("/book")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> bookRide(@RequestBody RideRequest request) {
        Ride ride = rideService.createRide(request);
        return ResponseEntity.ok(ride);
    }

    @PostMapping("/{rideId}/accept")
//    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<String> acceptRide(
            @PathVariable Long rideId,
            @RequestParam Long driverId) {

        rideService.acceptRide(rideId, driverId);
        return ResponseEntity.ok("Ride accepted successfully");
    }

    @PostMapping("/{rideId}/start")
//    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<String> startRide(@PathVariable("rideId") Long rideId){
        rideService.startRide(rideId);
        return ResponseEntity.ok("Ride started successfully");
    }

    @PostMapping("/{rideId}/complete")
//    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<String> completeRide(@PathVariable Long rideId,
                                               @RequestParam double distanceKm,
                                               @RequestParam double durationMinutes) {
        rideService.completeRide(rideId, distanceKm, durationMinutes);
        return ResponseEntity.ok("Ride completed and fare calculated.");
    }


    @PostMapping("/{rideId}/cancel")
//    @PreAuthorize("hasRole('DRIVER')")
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
//    @PreAuthorize("hasRole('DRIVER') or hasRole('USER')")
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
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Ride>> getUserRideHistory(@PathVariable Long userId){
        List<Ride> rides = rideService.getUserRideHistory(userId);
        return ResponseEntity.ok(rides);
    }

    @PostMapping("/driver/{driverId}/rides")
//    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<Ride>> getDriverRideHistory(@PathVariable Long driverId){
        List<Ride> rides = rideService.getDirverRideHistory(driverId);
        return ResponseEntity.ok(rides);
    }

    @PostMapping("/rate")
//    @PreAuthorize("hasRole('DRIVER') or hasRole('USER')")
    public ResponseEntity<?> rateRide(@RequestBody RideRatingRequest request){
        rideService.rateRide(request);
        return ResponseEntity.ok("rating submitted successfully");
    }

    @GetMapping("/user/{userId}/current")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCurrentRideForUser(@PathVariable Long userId) {
        return rideService.getCurrentRideForUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/driver/{driverId}/current")
//    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<?> getCurrentRideForDriver(@PathVariable Long driverId){
        return rideService.getCurrentRideForDriver(driverId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estimate")
    public ResponseEntity<Map<String, Object>> estimateFare(
            @RequestParam double pickupLat,
            @RequestParam double pickupLng,
            @RequestParam double dropLat,
            @RequestParam double dropLng
    ){
        double estimatedFare = rideService.estimateFare(pickupLat, pickupLng, dropLat, dropLng);
        Map<String, Object> response = new HashMap<>();
        response.put("estimatedFare", Math.round(estimatedFare * 100.0) / 100.0);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{rideId}/payment/success")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> confirmPayment(
            @PathVariable Long rideId,
            @RequestParam String razorpayPaymentId
    ) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.setPaymentStatus(paymentStatus.SUCCESS);
        ride.setTransactionId(razorpayPaymentId); // Razorpay payment ID
        rideRepository.save(ride);

        return ResponseEntity.ok("Payment confirmed");
    }

}
