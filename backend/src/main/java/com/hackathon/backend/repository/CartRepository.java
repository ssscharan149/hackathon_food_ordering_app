package com.hackathon.backend.repository;

import com.hackathon.backend.model.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserUserId(Long userId);
}
