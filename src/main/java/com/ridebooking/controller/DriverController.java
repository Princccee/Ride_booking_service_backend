package com.ridebooking.controller;


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
}
