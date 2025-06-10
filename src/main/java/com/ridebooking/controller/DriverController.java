package com.ridebooking.controller;


import com.ridebooking.dto.DriverLocationUpdateRequest;
import com.ridebooking.dto.DriverLoginRequest;
import com.ridebooking.dto.DriverRegisterRequest;
import com.ridebooking.model.Driver;
import com.ridebooking.security.JwtUtil;
import com.ridebooking.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/driver")
@RequiredArgsConstructor
public class DriverController {
    private final DriverService driverService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody DriverRegisterRequest request){
        Driver driver = driverService.register(request);
        return ResponseEntity.ok(driver);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody DriverLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        Driver driver = driverService.findByUsername(request.getUsername());
        String token = jwtUtil.generateToken(driver.getUsername());

        return ResponseEntity.ok().body("Bearer " + token);
    }

    @PostMapping("/{id}/availability")
    public ResponseEntity<?> driverAvailability(@PathVariable Long id){
        Driver driver = driverService.toggleAvailability(id);
        return ResponseEntity.ok(driver);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Driver>> getAvailableDrivers(){
        List<Driver> availableDrivers =  driverService.getAvailableDrivers();
        return ResponseEntity.ok(availableDrivers);
    }

    @PostMapping("/{driverId}/location")
    public ResponseEntity<String> updateLocation(
            @PathVariable Long driverId,
            @RequestBody DriverLocationUpdateRequest request) {

        driverService.updateDriverLocation(driverId, request.getLatitude(), request.getLongitude());
        return ResponseEntity.ok("Driver location updated");
    }
}
