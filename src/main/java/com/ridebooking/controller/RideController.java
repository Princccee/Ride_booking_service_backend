package com.ridebooking.controller;

import com.ridebooking.dto.RideRatingRequest;
import com.ridebooking.dto.RideRequest;
import com.ridebooking.model.Ride;
import com.ridebooking.model.paymentStatus;
import com.ridebooking.model.rideStatus;
import com.ridebooking.repository.RideRepository;
import com.ridebooking.service.RideService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("api/rides")
public class RideController {
    @Autowired
    private RideService rideService;

    private RideRepository rideRepository;

    @PostMapping("/book")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> bookRide(@RequestBody RideRequest request) {
        log.info("Ride booking requested : {}", request );
        try{
            Ride ride = rideService.createRide(request);
            log.info("Ride booked successfully: {}", ride.getId());
            return ResponseEntity.ok(ride);
        } catch (Exception e) {
            log.error("Ride booking failed ", e);
            return ResponseEntity.status(500).body("Error booking ride");
        }
    }

    @PostMapping("/{rideId}/accept")
//    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<String> acceptRide(
            @PathVariable Long rideId,
            @RequestParam Long driverId) {
        log.info("Ride {} accept initiated by driver {}", rideId, driverId);
        try{
            rideService.acceptRide(rideId, driverId);
            log.info("Ride accepted successfully");
            return ResponseEntity.ok("Ride accepted successfully");
        } catch (Exception e) {
            log.error("Ride acceptance failed");
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/{rideId}/start")
//    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<String> startRide(@PathVariable("rideId") Long rideId){
        log.info("Ride {} start initiated", rideId);
        try{
            rideService.startRide(rideId);
            log.info("Ride {} started successfully.", rideId);
            return ResponseEntity.ok("Ride started successfully");
        } catch (Exception e) {
            log.error("Ride start failed");
            return ResponseEntity.status(500).body("Ride start failed with " + e.getMessage());
//            throw new RuntimeException(e);
        }
    }

    @PostMapping("/{rideId}/complete")
//    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<String> completeRide(@PathVariable Long rideId,
                                               @RequestParam double distanceKm,
                                               @RequestParam double durationMinutes) {
        log.info("Complete ride {} with distance {} and duration {} initiated", rideId, distanceKm, durationMinutes);
        try{
            rideService.completeRide(rideId, distanceKm, durationMinutes);
            log.info("Ride {} completed successfully.", rideId);
            return ResponseEntity.ok("Ride completed successfully");
        } catch (Exception e) {
            log.error("Ride completion failed");
//            throw new RuntimeException(e);
            return ResponseEntity.status(500).body("Ride completion failed with " + e.getMessage());
        }
    }


    @PostMapping("/{rideId}/cancel")
//    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<?> cancelRide(@PathVariable Long rideId){
        log.info("Ride {} cancellation initiated.", rideId);
        try{
            Ride cancelRide = rideService.cancelRide(rideId);
            log.info("Ride {} cancelled successfully", rideId);
            return ResponseEntity.ok(cancelRide);
        }
        catch (RuntimeException e){
            log.error("Failed to cancel the ride {}", rideId);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{rideId}/track")
//    @PreAuthorize("hasRole('DRIVER') or hasRole('USER')")
    public ResponseEntity<?> trackRide(@PathVariable Long rideId){
        log.info("Ride {} status check initiated", rideId);
        try{
            rideStatus rideStatus = rideService.getRideStatus(rideId);
            log.info("Ride {} current status is {}", rideId, rideStatus);
            return ResponseEntity.ok(rideStatus);
        }
        catch (RuntimeException e){
            log.error("Failed to fetch the status of the ride");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/user/{userId}/rides")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Ride>> getUserRideHistory(@PathVariable Long userId){
        log.info("User {} ride history initiated", userId);
        try{
            List<Ride> rides = rideService.getUserRideHistory(userId);
            log.info("Ride history successfully fetched");
            return ResponseEntity.ok(rides);
        }
        catch (RuntimeException e){
            log.error("Failed to fetch the user's ride history");
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/driver/{driverId}/rides")
//    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<Ride>> getDriverRideHistory(@PathVariable Long driverId){
        log.info("Driver {} ride history fetch initiated", driverId);
        try{
            List<Ride> rides = rideService.getDirverRideHistory(driverId);
            log.info("Driver {} rides fetched successfully." , driverId);
            return ResponseEntity.ok(rides);
        } catch (Exception e) {
            log.error("Failed to fetch driver's ride history");
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/rate")
//    @PreAuthorize("hasRole('DRIVER') or hasRole('USER')")
    public ResponseEntity<?> rateRide(@RequestBody RideRatingRequest request){
        log.info("Rate the ride {}", request.getRideId());
        try{
            rideService.rateRide(request);
            log.info("Rating and feedback submitted successfully, with rating {} and feedback {}", request.getRating(), request.getFeedback() );
            return ResponseEntity.ok("Rating successfully submitted");
        } catch (Exception e) {
            log.error("Ride rating failed");
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/user/{userId}/current")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCurrentRideForUser(@PathVariable Long userId) {
        log.info("Get current active ride for the user if any");
        return rideService.getCurrentRideForUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/driver/{driverId}/current")
//    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<?> getCurrentRideForDriver(@PathVariable Long driverId){
        log.info("Get the current active ride for the driver if any");
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
        log.info("Estimate fare for the ride");
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
