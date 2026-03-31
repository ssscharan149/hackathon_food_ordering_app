package com.hackathon.backend.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.hackathon.backend.dto.MenuItemDTO;
import com.hackathon.backend.dto.MenuItemResponse;
import com.hackathon.backend.exceptions.ResourceNotFoundException;
import com.hackathon.backend.model.Category;
import com.hackathon.backend.model.MenuItem;
import com.hackathon.backend.model.Restaurant;
import com.hackathon.backend.repository.CategoryRepository;
import com.hackathon.backend.repository.MenuItemRepository;
import com.hackathon.backend.repository.RestaurantRepository;

@Service
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final ModelMapper modelMapper;

    public MenuItemServiceImpl(
            MenuItemRepository menuItemRepository,
            CategoryRepository categoryRepository,
            RestaurantRepository restaurantRepository,
            ModelMapper modelMapper
    ) {
        this.menuItemRepository = menuItemRepository;
        this.categoryRepository = categoryRepository;
        this.restaurantRepository = restaurantRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public MenuItemDTO addMenuItem(Long categoryId, Long restaurantId, MenuItemDTO menuItemDTO) {
        Category category = getCategoryById(categoryId);
        Restaurant restaurant = getRestaurantById(restaurantId);

        boolean alreadyExists = menuItemRepository.existsByNameIgnoreCaseAndCategoryCategoryIdAndRestaurantRestaurantId(
                menuItemDTO.getName(),
                categoryId,
                restaurantId
        );

        if (alreadyExists) {
            throw new IllegalArgumentException("Menu item already exists for this category and restaurant");
        }

        MenuItem menuItem = modelMapper.map(menuItemDTO, MenuItem.class);
        menuItem.setCategory(category);
        menuItem.setRestaurant(restaurant);

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        return mapToDto(savedMenuItem);
    }

    @Override
    public MenuItemResponse getAllMenuItems(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Pageable pageDetails = getPageable(pageNumber, pageSize, sortBy, sortOrder);
        Page<MenuItem> pageMenuItems = menuItemRepository.findAll(pageDetails);
        return buildResponse(pageMenuItems);
    }

    @Override
    public MenuItemDTO getMenuItemById(Long menuItemId) {
        MenuItem menuItem = getMenuItemByIdOrThrow(menuItemId);
        return mapToDto(menuItem);
    }

    @Override
    public MenuItemResponse getMenuItemsByCategory(
            Long categoryId,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder
    ) {
        Category category = getCategoryById(categoryId);
        Pageable pageDetails = getPageable(pageNumber, pageSize, sortBy, sortOrder);
        Page<MenuItem> pageMenuItems = menuItemRepository.findByCategory(category, pageDetails);
        return buildResponse(pageMenuItems);
    }

    @Override
    public MenuItemResponse searchMenuItemsByKeyword(
            String keyword,
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder
    ) {
        Pageable pageDetails = getPageable(pageNumber, pageSize, sortBy, sortOrder);
        Page<MenuItem> pageMenuItems = menuItemRepository.findByNameContainingIgnoreCase(keyword, pageDetails);
        return buildResponse(pageMenuItems);
    }

    @Override
    public MenuItemDTO updateMenuItem(Long menuItemId, MenuItemDTO menuItemDTO) {
        MenuItem menuItemFromDb = getMenuItemByIdOrThrow(menuItemId);

        Category category = getCategoryById(menuItemDTO.getCategoryId());
        Restaurant restaurant = getRestaurantById(menuItemDTO.getRestaurantId());

        menuItemFromDb.setName(menuItemDTO.getName());
        menuItemFromDb.setDescription(menuItemDTO.getDescription());
        menuItemFromDb.setImageUrl(menuItemDTO.getImageUrl());
        menuItemFromDb.setPrice(menuItemDTO.getPrice());
        menuItemFromDb.setIsAvailable(menuItemDTO.getIsAvailable());
        menuItemFromDb.setCategory(category);
        menuItemFromDb.setRestaurant(restaurant);

        MenuItem updatedMenuItem = menuItemRepository.save(menuItemFromDb);
        return mapToDto(updatedMenuItem);
    }

    @Override
    public MenuItemDTO deleteMenuItem(Long menuItemId) {
        MenuItem menuItem = getMenuItemByIdOrThrow(menuItemId);
        menuItemRepository.delete(menuItem);
        return mapToDto(menuItem);
    }

    private MenuItem getMenuItemByIdOrThrow(Long menuItemId) {
        return menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "menuItemId", menuItemId));
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
    }

    private Restaurant getRestaurantById(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", "restaurantId", restaurantId));
    }

    private Pageable getPageable(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(pageNumber, pageSize, sortByAndOrder);
    }

    private MenuItemResponse buildResponse(Page<MenuItem> pageMenuItems) {
        List<MenuItemDTO> menuItemDTOS = pageMenuItems.getContent()
                .stream()
                .map(this::mapToDto)
                .toList();

        MenuItemResponse menuItemResponse = new MenuItemResponse();
        menuItemResponse.setContent(menuItemDTOS);
        menuItemResponse.setPageNumber(pageMenuItems.getNumber());
        menuItemResponse.setPageSize(pageMenuItems.getSize());
        menuItemResponse.setTotalElements(pageMenuItems.getTotalElements());
        menuItemResponse.setTotalPages(pageMenuItems.getTotalPages());
        menuItemResponse.setLastPage(pageMenuItems.isLast());
        return menuItemResponse;
    }

    private MenuItemDTO mapToDto(MenuItem menuItem) {
        MenuItemDTO menuItemDTO = modelMapper.map(menuItem, MenuItemDTO.class);
        menuItemDTO.setCategoryId(menuItem.getCategory().getCategoryId());
        menuItemDTO.setRestaurantId(menuItem.getRestaurant().getRestaurantId());
        return menuItemDTO;
    }
}
