package com.hackathon.backend.repository;

import com.hackathon.backend.model.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartLookupRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<Cart> findByUserUserId(Long userId);
}
