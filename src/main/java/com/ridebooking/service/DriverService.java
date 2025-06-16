package com.ridebooking.service;

import com.ridebooking.dto.DriverRegisterRequest;
import com.ridebooking.model.Driver;
import com.ridebooking.model.driverStatus;
import com.ridebooking.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverService {

    @Autowired
    private final DriverRepository driverRepo;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public Driver register(DriverRegisterRequest request){
        //Create the driver entity
        Driver driver = Driver.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .vehicleNumber(request.getVehicleNumber())
                .vehicleModel(request.getVehicleModel())
                .licenceNumber(request.getLicenseNumber())
                .status(driverStatus.AVAILABLE)
                .build();
        //Save the record into the DB
        return driverRepo.save(driver);
    }

    public Driver findByUsername(String username){
        return driverRepo.findByUsername(username);
    }

    // This function enables drivers to mark that they are ready to accept rides or not
    public Driver toggleAvailability(Long driverId){
        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(()-> new RuntimeException("Driver doesn't exist"));

        // If the driver is already on a ride
        if(driver.getStatus() == driverStatus.ON_RIDE)
            throw new RuntimeException("Can't toggle rn, as driver status is ON_RIDE");

        // If the current state is AVAILABLE make it OFFLINE and vice-versa
        if(driver.getStatus() == driverStatus.AVAILABLE)
            driver.setStatus(driverStatus.OFFLINE);
        else if(driver.getStatus() == driverStatus.OFFLINE)
            driver.setStatus(driverStatus.AVAILABLE);

        return driverRepo.save(driver);
    }

    public List<Driver> getAvailableDrivers(){
        return driverRepo.findByStatus(driverStatus.AVAILABLE);
    }

    // function to keep updating the driver's lve location in the DB
    public void updateDriverLocation(Long driverId, Double lat, Double lon) {
        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        driver.setCurrentLatitude(lat);
        driver.setCurrentLongitude(lon);
        driverRepo.save(driver);
    }

    public void updateFcmToken(Long driverId, String token) {
        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        driver.setFcmToken(token);
        driverRepo.save(driver);
    }


}
