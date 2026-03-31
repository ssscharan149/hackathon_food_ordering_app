package com.hackathon.backend.controller;

import com.hackathon.backend.dto.OrderDTO;
import com.hackathon.backend.dto.OrderRequestDTO;
import com.hackathon.backend.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/me/orders")
    public ResponseEntity<OrderDTO> placeOrder(
            @Valid @RequestBody OrderRequestDTO orderRequestDTO
    ) {
        OrderDTO createdOrder = orderService.placeOrder(orderRequestDTO);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping("/me/orders")
    public ResponseEntity<List<OrderDTO>> getUserOrders() {
        List<OrderDTO> orders = orderService.getUserOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        OrderDTO orderDTO = orderService.getOrderById(orderId);
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }
}
