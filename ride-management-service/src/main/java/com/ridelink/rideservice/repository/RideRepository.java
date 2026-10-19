package com.ridelink.rideservice.repository;

import com.ridelink.rideservice.model.Ride;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RideRepository extends MongoRepository<Ride, String> {

}