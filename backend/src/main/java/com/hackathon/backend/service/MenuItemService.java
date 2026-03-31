package com.hackathon.backend.service;

import com.hackathon.backend.dto.MenuItemDTO;
import com.hackathon.backend.dto.MenuItemResponse;

public interface MenuItemService {

    MenuItemDTO addMenuItem(Long categoryId, Long restaurantId, MenuItemDTO menuItemDTO);

    MenuItemResponse getAllMenuItems(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    MenuItemDTO getMenuItemById(Long menuItemId);

    MenuItemResponse getMenuItemsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    MenuItemResponse searchMenuItemsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    MenuItemDTO updateMenuItem(Long menuItemId, MenuItemDTO menuItemDTO);

    MenuItemDTO deleteMenuItem(Long menuItemId);
}
