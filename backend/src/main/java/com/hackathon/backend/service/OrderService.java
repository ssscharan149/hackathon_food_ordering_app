package com.hackathon.backend.service;

import com.hackathon.backend.dto.OrderDTO;
import com.hackathon.backend.dto.OrderRequestDTO;
import java.util.List;

public interface OrderService {

    OrderDTO placeOrder(Long userId, OrderRequestDTO orderRequestDTO);

    List<OrderDTO> getUserOrders(Long userId);

    OrderDTO getOrderById(Long orderId);
}
