package com.hackathon.backend.controller;

import com.hackathon.backend.dto.OrderDTO;
import com.hackathon.backend.dto.OrderRequestDTO;
import com.hackathon.backend.security.UserDetailsImpl;
import com.hackathon.backend.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/users/{userId}/orders")
    public ResponseEntity<OrderDTO> placeOrder(
            @PathVariable Long userId,
            @Valid @RequestBody OrderRequestDTO orderRequestDTO,
            Authentication authentication
    ) {
        validateAuthenticatedUser(userId, authentication);
        OrderDTO createdOrder = orderService.placeOrder(userId, orderRequestDTO);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<List<OrderDTO>> getUserOrders(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        validateAuthenticatedUser(userId, authentication);
        List<OrderDTO> orders = orderService.getUserOrders(userId);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        OrderDTO orderDTO = orderService.getOrderById(orderId);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }

    private void validateAuthenticatedUser(Long userId, Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails && !userId.equals(userDetails.getUserId())) {
            throw new AccessDeniedException("You can only access your own orders");
        }
    }
}
