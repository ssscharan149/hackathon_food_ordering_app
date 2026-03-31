package com.hackathon.backend.repository;

import com.hackathon.backend.model.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartCartIdAndMenuItemMenuItemId(Long cartId, Long menuItemId);

    void deleteByCartCartIdAndMenuItemMenuItemId(Long cartId, Long menuItemId);

    @EntityGraph(attributePaths = {"cart", "menuItem", "menuItem.restaurant"})
    List<CartItem> findByCartCartId(Long cartId);
}
