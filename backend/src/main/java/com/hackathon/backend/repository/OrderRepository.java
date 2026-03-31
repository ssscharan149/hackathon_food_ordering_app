package com.hackathon.backend.repository;

import com.hackathon.backend.model.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"user", "restaurant"})
    List<Order> findByUserUserIdOrderByOrderIdDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "restaurant"})
    Optional<Order> findByOrderId(Long orderId);
}
