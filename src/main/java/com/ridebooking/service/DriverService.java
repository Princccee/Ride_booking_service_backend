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

    public Driver toggleAvailability(Long driverId){
        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(()-> new RuntimeException("Driver doesn't exist"));

        if(driver.getStatus() == driverStatus.ON_RIDE)
            throw new RuntimeException("Can't toggle rn, as driver status is ON_RIDE");

        if(driver.getStatus() == driverStatus.AVAILABLE)
            driver.setStatus(driverStatus.OFFLINE);
        else if(driver.getStatus() == driverStatus.OFFLINE)
            driver.setStatus(driverStatus.AVAILABLE);

        return driverRepo.save(driver);
    }

    public List<Driver> getAvailableDrivers(){
        return driverRepo.findByStatus(driverStatus.AVAILABLE);
    }

}
