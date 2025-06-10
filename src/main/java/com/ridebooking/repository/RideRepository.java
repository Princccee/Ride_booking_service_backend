package com.ridebooking.repository;

import com.ridebooking.model.Driver;
import com.ridebooking.model.Ride;
import com.ridebooking.model.User;
import com.ridebooking.model.rideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByRiderId(Long userId);

    List<Ride> findByDriverId(Long driverId);

    Optional<Ride> findByRiderAndStatusIn(User rider, List<rideStatus> statuses);

    Optional<Ride> findByDriverAndStatusIn(Driver driver, List<rideStatus> statuses);
}
