package com.hackathon.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hackathon.backend.model.Category;
import com.hackathon.backend.model.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    Page<MenuItem> findByCategory(Category category, Pageable pageable);

    Page<MenuItem> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    boolean existsByNameIgnoreCaseAndCategoryCategoryIdAndRestaurantRestaurantId(
            String name,
            Long categoryId,
            Long restaurantId
    );
}
