package com.hackathon.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.backend.model.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}
