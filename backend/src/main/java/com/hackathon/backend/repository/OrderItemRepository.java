package com.hackathon.backend.repository;

import com.hackathon.backend.model.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @EntityGraph(attributePaths = {"menuItem", "order"})
    List<OrderItem> findByOrderOrderId(Long orderId);

    @EntityGraph(attributePaths = {"menuItem", "order"})
    List<OrderItem> findByOrderOrderIdIn(List<Long> orderIds);
}
