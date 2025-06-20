package com.ridebooking.controller;


import com.ridebooking.model.Driver;
import com.ridebooking.model.User;
import com.ridebooking.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return authService.register(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credentials) {
        return authService.login(credentials.get("username"), credentials.get("password"));
    }

    @PostMapping("/registerDriver")
    public String registerDriver(@RequestBody Driver driver){
        return authService.registerDriver(driver);
    }

    @PostMapping("/loginDriver")
    public String loginDriver(@RequestBody Map<String, String> credentials){
        return authService.loginDriver(credentials.get("username"), credentials.get("password"));
    }
}
