package com.hackathon.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hackathon.backend.dto.RestaurantDTO;
import com.hackathon.backend.dto.RestaurantRequestDTO;
import com.hackathon.backend.exceptions.ResourceNotFoundException;
import com.hackathon.backend.model.Restaurant;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.RestaurantRepository;
import com.hackathon.backend.security.AuthenticatedUserService;

@Service
public class RestaurantServiceImpl {

    private final RestaurantRepository restaurantRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public RestaurantServiceImpl(
            RestaurantRepository restaurantRepository,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.restaurantRepository = restaurantRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    public RestaurantDTO createRestaurant(RestaurantRequestDTO restaurantRequestDTO) {
        User user = authenticatedUserService.getCurrentUser();
        Long userId = user.getUserId();

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
        restaurant.setPosterUrl(trimToNull(restaurantRequestDTO.getPosterUrl()));
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

    public List<RestaurantDTO> getRestaurantsByUserId() {
        Long userId = authenticatedUserService.getCurrentUser().getUserId();
        return restaurantRepository.findByCreatedByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public RestaurantDTO updateRestaurant(
            Long restaurantId,
            RestaurantRequestDTO restaurantRequestDTO
    ) {
        Restaurant restaurant = getRestaurantByIdOrThrow(restaurantId);
        validateRestaurantOwnership(restaurant);
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
        restaurant.setPosterUrl(trimToNull(restaurantRequestDTO.getPosterUrl()));
        restaurant.setLocation(updatedLocation);

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return mapToDTO(updatedRestaurant);
    }

    public String deleteRestaurant(Long restaurantId) {
        Restaurant restaurant = getRestaurantByIdOrThrow(restaurantId);
        validateRestaurantOwnership(restaurant);
        restaurantRepository.delete(restaurant);
        return "Restaurant deleted successfully with restaurantId: " + restaurantId;
    }

    private Restaurant getRestaurantByIdOrThrow(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "restaurantId", restaurantId));
    }

    private void validateRestaurantOwnership(Restaurant restaurant) {
        User authenticatedUser = authenticatedUserService.getCurrentUser();
        if (!restaurant.getCreatedBy().getUserId().equals(authenticatedUser.getUserId())) {
            throw new AccessDeniedException("You can only modify your own restaurants");
        }
    }

    private RestaurantDTO mapToDTO(Restaurant restaurant) {
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setRestaurantId(restaurant.getRestaurantId());
        restaurantDTO.setName(restaurant.getName());
        restaurantDTO.setDescription(restaurant.getDescription());
        restaurantDTO.setPosterUrl(restaurant.getPosterUrl());
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
