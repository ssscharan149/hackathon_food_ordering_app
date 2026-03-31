package com.hackathon.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.backend.dto.CartDTO;
import com.hackathon.backend.dto.CartItemDTO;
import com.hackathon.backend.exceptions.ResourceNotFoundException;
import com.hackathon.backend.model.Cart;
import com.hackathon.backend.model.CartItem;
import com.hackathon.backend.model.MenuItem;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.CartItemRepository;
import com.hackathon.backend.repository.CartRepository;
import com.hackathon.backend.repository.MenuItemRepository;
import com.hackathon.backend.repository.UserRepository;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    public CartServiceImpl(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            MenuItemRepository menuItemRepository,
            UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CartDTO addMenuItemToCart(Authentication authentication, Long menuItemId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getOrCreateCart(authentication);
        MenuItem menuItem = getAvailableMenuItem(menuItemId);

        CartItem cartItem = cartItemRepository.findByCartCartIdAndMenuItemMenuItemId(cart.getCartId(), menuItemId)
                .orElseGet(() -> {
                    CartItem newCartItem = new CartItem();
                    newCartItem.setCart(cart);
                    newCartItem.setMenuItem(menuItem);
                    newCartItem.setQuantity(0);
                    newCartItem.setProductPrice(menuItem.getPrice());
                    cart.getCartItems().add(newCartItem);
                    return newCartItem;
                });

        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItem.setProductPrice(menuItem.getPrice());
        cart.setTotalPrice(cart.getTotalPrice().add(menuItem.getPrice().multiply(BigDecimal.valueOf(quantity))));

        cartItemRepository.save(cartItem);
        Cart savedCart = cartRepository.save(cart);
        return mapCartToDto(savedCart);
    }

    @Override
    public List<CartDTO> getAllCarts() {
        return cartRepository.findAll()
                .stream()
                .map(this::mapCartToDto)
                .toList();
    }

    @Override
    public CartDTO getMyCart(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Cart cart = cartRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", user.getUserId()));
        return mapCartToDto(cart);
    }

    @Override
    @Transactional
    public CartDTO updateMenuItemQuantityInCart(Authentication authentication, Long menuItemId, Integer quantityChange) {
        if (quantityChange == null || quantityChange == 0) {
            throw new IllegalArgumentException("Quantity change must not be 0");
        }

        User user = getAuthenticatedUser(authentication);
        Cart cart = cartRepository.findByUserUserId(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", user.getUserId()));

        CartItem cartItem = cartItemRepository.findByCartCartIdAndMenuItemMenuItemId(cart.getCartId(), menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "menuItemId", menuItemId));

        int updatedQuantity = cartItem.getQuantity() + quantityChange;
        BigDecimal priceDelta = cartItem.getProductPrice().multiply(BigDecimal.valueOf(Math.abs(quantityChange)));

        if (updatedQuantity <= 0) {
            cart.setTotalPrice(cart.getTotalPrice().subtract(
                    cartItem.getProductPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            ));
            cart.getCartItems().removeIf(item -> item.getCartItemId().equals(cartItem.getCartItemId()));
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(updatedQuantity);
            if (quantityChange > 0) {
                MenuItem menuItem = getAvailableMenuItem(menuItemId);
                cartItem.setProductPrice(menuItem.getPrice());
                cart.setTotalPrice(cart.getTotalPrice().add(priceDelta));
            } else {
                cart.setTotalPrice(cart.getTotalPrice().subtract(priceDelta));
            }
            cartItemRepository.save(cartItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return mapCartToDto(savedCart);
    }

    @Override
    @Transactional
    public String deleteMenuItemFromCart(Authentication authentication, Long cartId, Long menuItemId) {
        User user = getAuthenticatedUser(authentication);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        if (!cart.getUser().getUserId().equals(user.getUserId())) {
            throw new BadCredentialsException("You can access only your own cart");
        }

        CartItem cartItem = cartItemRepository.findByCartCartIdAndMenuItemMenuItemId(cartId, menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "menuItemId", menuItemId));

        cart.setTotalPrice(cart.getTotalPrice().subtract(
                cartItem.getProductPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
        ));
        cart.getCartItems().removeIf(item -> item.getCartItemId().equals(cartItem.getCartItemId()));
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);

        return "Menu item " + cartItem.getMenuItem().getName() + " removed from the cart";
    }

    private Cart getOrCreateCart(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return cartRepository.findByUserUserId(user.getUserId())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    cart.setTotalPrice(BigDecimal.ZERO);
                    return cartRepository.save(cart);
                });
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new BadCredentialsException("Authentication is required");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("User not found"));
    }

    private MenuItem getAvailableMenuItem(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "menuItemId", menuItemId));

        if (!Boolean.TRUE.equals(menuItem.getIsAvailable())) {
            throw new IllegalArgumentException("Menu item is not available");
        }

        return menuItem;
    }

    private CartDTO mapCartToDto(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(cart.getCartId());
        cartDTO.setUserId(cart.getUser().getUserId());
        cartDTO.setTotalPrice(cart.getTotalPrice());
        cartDTO.setCartItems(cart.getCartItems().stream()
                .map(this::mapCartItemToDto)
                .toList());
        return cartDTO;
    }

    private CartItemDTO mapCartItemToDto(CartItem cartItem) {
        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setCartItemId(cartItem.getCartItemId());
        cartItemDTO.setQuantity(cartItem.getQuantity());
        cartItemDTO.setProductPrice(cartItem.getProductPrice());
        cartItemDTO.setMenuItemId(cartItem.getMenuItem().getMenuItemId());
        cartItemDTO.setMenuItemName(cartItem.getMenuItem().getName());
        cartItemDTO.setImageUrl(cartItem.getMenuItem().getImageUrl());
        return cartItemDTO;
    }
}
