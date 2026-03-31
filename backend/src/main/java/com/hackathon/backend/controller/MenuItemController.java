package com.hackathon.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.backend.config.AppConstants;
import com.hackathon.backend.dto.MenuItemDTO;
import com.hackathon.backend.dto.MenuItemResponse;
import com.hackathon.backend.service.MenuItemService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @PostMapping("/admin/categories/{categoryId}/restaurants/{restaurantId}/menuItems")
    public ResponseEntity<MenuItemDTO> addMenuItem(
            @PathVariable Long categoryId,
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemDTO menuItemDTO
    ) {
        MenuItemDTO savedMenuItem = menuItemService.addMenuItem(categoryId, restaurantId, menuItemDTO);
        return new ResponseEntity<>(savedMenuItem, HttpStatus.CREATED);
    }

    @GetMapping("/public/menuItems")
    public ResponseEntity<MenuItemResponse> getAllMenuItems(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_MENU_ITEMS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        MenuItemResponse menuItemResponse = menuItemService.getAllMenuItems(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(menuItemResponse, HttpStatus.OK);
    }

    @GetMapping("/admin/menuItems")
    public ResponseEntity<MenuItemResponse> getAllMenuItemsForAdmin(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_MENU_ITEMS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        MenuItemResponse menuItemResponse = menuItemService.getAllMenuItems(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(menuItemResponse, HttpStatus.OK);
    }

    @GetMapping("/public/menuItems/{menuItemId}")
    public ResponseEntity<MenuItemDTO> getMenuItemById(@PathVariable Long menuItemId) {
        MenuItemDTO menuItemDTO = menuItemService.getMenuItemById(menuItemId);
        return new ResponseEntity<>(menuItemDTO, HttpStatus.OK);
    }

    @GetMapping("/public/categories/{categoryId}/menuItems")
    public ResponseEntity<MenuItemResponse> getMenuItemsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_MENU_ITEMS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        MenuItemResponse menuItemResponse = menuItemService.getMenuItemsByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(menuItemResponse, HttpStatus.OK);
    }

    @GetMapping("/public/menuItems/keyword/{keyword}")
    public ResponseEntity<MenuItemResponse> getMenuItemsByKeyword(
            @PathVariable String keyword,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_MENU_ITEMS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        MenuItemResponse menuItemResponse = menuItemService.searchMenuItemsByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(menuItemResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/menuItems/{menuItemId}")
    public ResponseEntity<MenuItemDTO> updateMenuItem(
            @PathVariable Long menuItemId,
            @Valid @RequestBody MenuItemDTO menuItemDTO
    ) {
        MenuItemDTO updatedMenuItem = menuItemService.updateMenuItem(menuItemId, menuItemDTO);
        return new ResponseEntity<>(updatedMenuItem, HttpStatus.OK);
    }

    @DeleteMapping("/admin/menuItems/{menuItemId}")
    public ResponseEntity<MenuItemDTO> deleteMenuItem(@PathVariable Long menuItemId) {
        MenuItemDTO deletedMenuItem = menuItemService.deleteMenuItem(menuItemId);
        return new ResponseEntity<>(deletedMenuItem, HttpStatus.OK);
    }
}
