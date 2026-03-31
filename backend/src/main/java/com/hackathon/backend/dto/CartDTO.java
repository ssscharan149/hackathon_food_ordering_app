package com.hackathon.backend.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartDTO {

    private Long cartId;
    private Long userId;
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private List<CartItemDTO> cartItems = new ArrayList<>();

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<CartItemDTO> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemDTO> cartItems) {
        this.cartItems = cartItems;
    }
}
