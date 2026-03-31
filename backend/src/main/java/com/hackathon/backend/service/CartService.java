package com.hackathon.backend.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.hackathon.backend.dto.CartDTO;

public interface CartService {

    CartDTO addMenuItemToCart(Authentication authentication, Long menuItemId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getMyCart(Authentication authentication);

    CartDTO updateMenuItemQuantityInCart(Authentication authentication, Long menuItemId, Integer quantityChange);

    String deleteMenuItemFromCart(Authentication authentication, Long cartId, Long menuItemId);
}
