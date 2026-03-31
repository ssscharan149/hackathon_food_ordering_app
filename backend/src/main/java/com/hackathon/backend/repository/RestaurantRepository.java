package com.hackathon.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.backend.model.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByCreatedByUserId(Long userId);

    boolean existsByNameIgnoreCaseAndLocationIgnoreCaseAndCreatedByUserId(String name, String location, Long userId);
}
