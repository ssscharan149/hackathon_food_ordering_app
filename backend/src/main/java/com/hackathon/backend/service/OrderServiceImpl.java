package com.hackathon.backend.service;

import com.hackathon.backend.dto.OrderDTO;
import com.hackathon.backend.dto.OrderItemDTO;
import com.hackathon.backend.dto.OrderRequestDTO;
import com.hackathon.backend.exceptions.ResourceNotFoundException;
import com.hackathon.backend.model.Cart;
import com.hackathon.backend.model.CartItem;
import com.hackathon.backend.model.MenuItem;
import com.hackathon.backend.model.Order;
import com.hackathon.backend.model.OrderItem;
import com.hackathon.backend.model.Restaurant;
import com.hackathon.backend.model.User;
import com.hackathon.backend.repository.CartItemRepository;
import com.hackathon.backend.repository.CartLookupRepository;
import com.hackathon.backend.repository.OrderItemRepository;
import com.hackathon.backend.repository.OrderRepository;
import com.hackathon.backend.security.AuthenticatedUserService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartLookupRepository cartLookupRepository;
    private final CartItemRepository cartItemRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartLookupRepository cartLookupRepository,
            CartItemRepository cartItemRepository,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartLookupRepository = cartLookupRepository;
        this.cartItemRepository = cartItemRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Override
    @Transactional
    public OrderDTO placeOrder(OrderRequestDTO orderRequestDTO) {
        User user = authenticatedUserService.getCurrentUser();
        Long userId = user.getUserId();

        Cart cart = cartLookupRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        List<CartItem> cartItems = cartItemRepository.findByCartCartId(cart.getCartId());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Restaurant restaurant = validateAndGetRestaurant(cartItems);
        BigDecimal totalAmount = calculateTotalAmount(cartItems);

        Order order = new Order();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setOrderStatus("PLACED");
        order.setTotalAmount(totalAmount);
        order.setDeliveryAddress(orderRequestDTO.getDeliveryAddress().trim());

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setMenuItem(cartItem.getMenuItem());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getMenuItem().getPrice());
            orderItems.add(orderItem);
        }

        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);
        cartItemRepository.deleteAll(cartItems);

        return mapToOrderDtoFromEntities(savedOrder, savedOrderItems);
    }

    @Override
    public List<OrderDTO> getUserOrders() {
        Long userId = authenticatedUserService.getCurrentUser().getUserId();
        List<Order> orders = orderRepository.findByUserUserIdOrderByOrderIdDesc(userId);
        if (orders.isEmpty()) {
            return List.of();
        }

        List<Long> orderIds = orders.stream()
                .map(Order::getOrderId)
                .toList();

        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderIdIn(orderIds);
        Map<Long, List<OrderItemDTO>> itemsByOrderId = new LinkedHashMap<>();

        for (OrderItem orderItem : orderItems) {
            itemsByOrderId
                    .computeIfAbsent(orderItem.getOrder().getOrderId(), ignored -> new ArrayList<>())
                    .add(mapToOrderItemDto(orderItem));
        }

        return orders.stream()
                .map(order -> mapToOrderDto(order, itemsByOrderId.getOrDefault(order.getOrderId(), List.of())))
                .toList();
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        validateOrderOwnership(order);
        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(orderId);
        return mapToOrderDtoFromEntities(order, orderItems);
    }

    private void validateOrderOwnership(Order order) {
        User authenticatedUser = authenticatedUserService.getCurrentUser();
        if (!order.getUser().getUserId().equals(authenticatedUser.getUserId())) {
            throw new AccessDeniedException("You can only access your own orders");
        }
    }

    private Restaurant validateAndGetRestaurant(List<CartItem> cartItems) {
        Restaurant restaurant = cartItems.get(0).getMenuItem().getRestaurant();

        for (CartItem cartItem : cartItems) {
            MenuItem menuItem = cartItem.getMenuItem();

            if (Boolean.FALSE.equals(menuItem.getIsAvailable())) {
                throw new IllegalArgumentException("Menu item is not available: " + menuItem.getName());
            }

            if (!restaurant.getRestaurantId().equals(menuItem.getRestaurant().getRestaurantId())) {
                throw new IllegalArgumentException("All cart items must belong to the same restaurant");
            }
        }

        return restaurant;
    }

    private BigDecimal calculateTotalAmount(List<CartItem> cartItems) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            BigDecimal itemTotal = cartItem.getMenuItem().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        return totalAmount;
    }

    private OrderDTO mapToOrderDtoFromEntities(Order order, List<OrderItem> orderItems) {
        List<OrderItemDTO> orderItemDTOS = orderItems.stream()
                .map(this::mapToOrderItemDto)
                .toList();
        return mapToOrderDto(order, orderItemDTOS);
    }

    private OrderDTO mapToOrderDto(Order order, List<OrderItemDTO> orderItems) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderId(order.getOrderId());
        orderDTO.setUserId(order.getUser().getUserId());
        orderDTO.setRestaurantId(order.getRestaurant().getRestaurantId());
        orderDTO.setRestaurantName(order.getRestaurant().getName());
        orderDTO.setOrderStatus(order.getOrderStatus());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setDeliveryAddress(order.getDeliveryAddress());
        orderDTO.setOrderItems(orderItems);
        return orderDTO;
    }

    private OrderItemDTO mapToOrderItemDto(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setOrderItemId(orderItem.getOrderItemId());
        orderItemDTO.setMenuItemId(orderItem.getMenuItem().getMenuItemId());
        orderItemDTO.setMenuItemName(orderItem.getMenuItem().getName());
        orderItemDTO.setQuantity(orderItem.getQuantity());
        orderItemDTO.setPrice(orderItem.getPrice());
        return orderItemDTO;
    }
}
