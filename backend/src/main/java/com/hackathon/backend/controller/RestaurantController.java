package com.hackathon.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.backend.dto.RestaurantDTO;
import com.hackathon.backend.dto.RestaurantRequestDTO;
import com.hackathon.backend.service.RestaurantServiceImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class RestaurantController {

    private final RestaurantServiceImpl restaurantService;

    public RestaurantController(RestaurantServiceImpl restaurantService) {
        this.restaurantService = restaurantService;
    }

    @PostMapping("/users/{userId}/restaurants")
    public ResponseEntity<RestaurantDTO> createRestaurant(@PathVariable Long userId,
                                                          @Valid @RequestBody RestaurantRequestDTO restaurantRequestDTO) {
        RestaurantDTO savedRestaurant = restaurantService.createRestaurant(userId, restaurantRequestDTO);
        return new ResponseEntity<>(savedRestaurant, HttpStatus.CREATED);
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantDTO>> getRestaurants() {
        List<RestaurantDTO> restaurantList = restaurantService.getRestaurants();
        return new ResponseEntity<>(restaurantList, HttpStatus.OK);
    }

    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<RestaurantDTO> getRestaurantById(@PathVariable Long restaurantId) {
        RestaurantDTO restaurantDTO = restaurantService.getRestaurantById(restaurantId);
        return new ResponseEntity<>(restaurantDTO, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}/restaurants")
    public ResponseEntity<List<RestaurantDTO>> getRestaurantsByUserId(@PathVariable Long userId) {
        List<RestaurantDTO> restaurantList = restaurantService.getRestaurantsByUserId(userId);
        return new ResponseEntity<>(restaurantList, HttpStatus.OK);
    }

    @PutMapping("/restaurants/{restaurantId}")
    public ResponseEntity<RestaurantDTO> updateRestaurant(@PathVariable Long restaurantId,
                                                          @Valid @RequestBody RestaurantRequestDTO restaurantRequestDTO) {
        RestaurantDTO updatedRestaurant = restaurantService.updateRestaurant(restaurantId, restaurantRequestDTO);
        return new ResponseEntity<>(updatedRestaurant, HttpStatus.OK);
    }

    @DeleteMapping("/restaurants/{restaurantId}")
    public ResponseEntity<String> deleteRestaurant(@PathVariable Long restaurantId) {
        String status = restaurantService.deleteRestaurant(restaurantId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
