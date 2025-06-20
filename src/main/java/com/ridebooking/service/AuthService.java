package com.ridebooking.service;

import com.ridebooking.model.Driver;
import com.ridebooking.model.User;
import com.ridebooking.repository.DriverRepository;
import com.ridebooking.repository.UserRepository;
import com.ridebooking.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String register(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.save(user);
        return "User registered successfully";
    }

    public String registerDriver(Driver driver){
        driver.setPassword(passwordEncoder.encode(driver.getPassword()));
        driverRepository.save(driver);
        return "Driver registered successfully";
    }

    public String login(String username, String password){
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(passwordEncoder.matches(password, user.getPassword()))
            return jwtUtil.generateToken(user.getEmail());
        else
            throw new RuntimeException("Invalid Password");
    }

    public String loginDriver(String username, String password){
        Driver driver = driverRepository.findByUsername(username);

        if(passwordEncoder.matches(password, driver.getPassword()))
            return jwtUtil.generateToken(driver.getEmail());
        else
            throw new RuntimeException("Invalid Password");
    }
}
