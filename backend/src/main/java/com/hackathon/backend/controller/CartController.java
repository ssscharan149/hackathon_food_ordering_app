package com.hackathon.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.backend.dto.CartDTO;
import com.hackathon.backend.service.CartService;

@RestController
@RequestMapping("/api")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/carts/menuItems/{menuItemId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addMenuItemToCart(
            Authentication authentication,
            @PathVariable Long menuItemId,
            @PathVariable Integer quantity
    ) {
        CartDTO cartDTO = cartService.addMenuItemToCart(authentication, menuItemId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    @GetMapping({"/carts", "/getcarts"})
    public ResponseEntity<List<CartDTO>> getCarts() {
        List<CartDTO> cartDTOs = cartService.getAllCarts();
        return new ResponseEntity<>(cartDTOs, HttpStatus.OK);
    }

    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDTO> getMyCart(Authentication authentication) {
        CartDTO cartDTO = cartService.getMyCart(authentication);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @PutMapping("/cart/menuItems/{menuItemId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCartMenuItem(
            Authentication authentication,
            @PathVariable Long menuItemId,
            @PathVariable String operation
    ) {
        int quantityChange = operation.equalsIgnoreCase("delete") ? -1 : 1;
        CartDTO cartDTO = cartService.updateMenuItemQuantityInCart(authentication, menuItemId, quantityChange);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping("/carts/{cartId}/menuItems/{menuItemId}")
    public ResponseEntity<String> deleteMenuItemFromCart(
            Authentication authentication,
            @PathVariable Long cartId,
            @PathVariable Long menuItemId
    ) {
        String status = cartService.deleteMenuItemFromCart(authentication, cartId, menuItemId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
