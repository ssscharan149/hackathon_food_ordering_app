package com.hackathon.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hackathon.backend.dto.RestaurantDTO;
import com.hackathon.backend.dto.RestaurantRequestDTO;
import com.hackathon.backend.exceptions.ResourceNotFoundException;
import com.hackathon.backend.model.Restaurant;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.RestaurantRepository;
import com.hackathon.backend.repository.UserRepository;

@Service
public class RestaurantServiceImpl {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public RestaurantDTO createRestaurant(Long userId, RestaurantRequestDTO restaurantRequestDTO) {
        User user = getUserById(userId);

        if (restaurantRepository.existsByNameIgnoreCaseAndLocationIgnoreCaseAndCreatedByUserId(
                restaurantRequestDTO.getName(), restaurantRequestDTO.getLocation(), userId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Restaurant with the same name and location already exists for this user"
            );
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantRequestDTO.getName().trim());
        restaurant.setDescription(trimToNull(restaurantRequestDTO.getDescription()));
        restaurant.setLocation(restaurantRequestDTO.getLocation().trim());
        restaurant.setCreatedBy(user);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return mapToDTO(savedRestaurant);
    }

    public List<RestaurantDTO> getRestaurants() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public RestaurantDTO getRestaurantById(Long restaurantId) {
        return mapToDTO(getRestaurantByIdOrThrow(restaurantId));
    }

    public List<RestaurantDTO> getRestaurantsByUserId(Long userId) {
        getUserById(userId);
        return restaurantRepository.findByCreatedByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public RestaurantDTO updateRestaurant(Long restaurantId, RestaurantRequestDTO restaurantRequestDTO) {
        Restaurant restaurant = getRestaurantByIdOrThrow(restaurantId);
        String updatedName = restaurantRequestDTO.getName().trim();
        String updatedLocation = restaurantRequestDTO.getLocation().trim();

        boolean duplicateExists = restaurantRepository
                .findByCreatedByUserId(restaurant.getCreatedBy().getUserId())
                .stream()
                .anyMatch(existingRestaurant ->
                        !existingRestaurant.getRestaurantId().equals(restaurantId)
                                && existingRestaurant.getName().equalsIgnoreCase(updatedName)
                                && existingRestaurant.getLocation().equalsIgnoreCase(updatedLocation));

        if (duplicateExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another restaurant with the same name and location already exists for this user"
            );
        }

        restaurant.setName(updatedName);
        restaurant.setDescription(trimToNull(restaurantRequestDTO.getDescription()));
        restaurant.setLocation(updatedLocation);

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return mapToDTO(updatedRestaurant);
    }

    public String deleteRestaurant(Long restaurantId) {
        Restaurant restaurant = getRestaurantByIdOrThrow(restaurantId);
        restaurantRepository.delete(restaurant);
        return "Restaurant deleted successfully with restaurantId: " + restaurantId;
    }

    private Restaurant getRestaurantByIdOrThrow(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "restaurantId", restaurantId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
    }

    private RestaurantDTO mapToDTO(Restaurant restaurant) {
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setRestaurantId(restaurant.getRestaurantId());
        restaurantDTO.setName(restaurant.getName());
        restaurantDTO.setDescription(restaurant.getDescription());
        restaurantDTO.setLocation(restaurant.getLocation());
        restaurantDTO.setCreatedByUserId(restaurant.getCreatedBy().getUserId());
        restaurantDTO.setCreatedByUsername(restaurant.getCreatedBy().getUsername());
        return restaurantDTO;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
