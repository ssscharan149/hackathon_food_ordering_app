package com.hackathon.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.backend.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartCartIdAndMenuItemMenuItemId(Long cartId, Long menuItemId);

    void deleteByCartCartIdAndMenuItemMenuItemId(Long cartId, Long menuItemId);
}
