package com.jdmart.service;

import com.jdmart.dto.*;
import com.jdmart.exception.BadRequestException;
import com.jdmart.exception.ResourceNotFoundException;
import com.jdmart.model.*;
import com.jdmart.repository.CartRepository;
import com.jdmart.repository.OrderRepository;
import com.jdmart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final AuthService authService;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository,
                        CartService cartService,
                        AuthService authService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.authService = authService;
    }

    @Transactional
    public OrderDto placeOrder(CheckoutRequest request) {
        User user = authService.getAuthenticatedUser();
        Cart cart = cartService.getOrCreateCartForUser(user);

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty. Please add items before checking out.");
        }

        CartDto cartDto = cartService.mapToCartDto(cart);

        // Validate stock availability for each item
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product '" + product.getName() + "'. Available: " + product.getStockQuantity());
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setShippingFullName(request.getFullName());
        order.setShippingMobile(request.getMobile());
        order.setShippingHouseBuilding(request.getHouseBuilding());
        order.setShippingStreet(request.getStreet());
        order.setShippingCity(request.getCity());
        order.setShippingState(request.getState());
        order.setShippingPincode(request.getPincode());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatus.ORDERED);

        order.setSubtotal(cartDto.getSubtotal());
        order.setDeliveryCharge(cartDto.getDeliveryCharge());
        order.setDiscount(cartDto.getDiscount());
        order.setTotalAmount(cartDto.getTotalAmount());

        // Create order items & decrement product stock
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    product.getName(),
                    product.getImageUrl(),
                    product.getSellingPrice(),
                    item.getQuantity()
            );
            order.addItem(orderItem);

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        Order savedOrder = orderRepository.save(order);

        // Clear cart after successful order creation
        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders() {
        User user = authService.getAuthenticatedUser();
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        User user = authService.getAuthenticatedUser();
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN);
        if (!order.getUser().getId().equals(user.getId()) && !isAdmin) {
            throw new BadRequestException("You do not have permission to view this order");
        }

        return mapToDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::mapToDto);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(status);
        if (status == OrderStatus.DELIVERED) {
            order.setPaymentStatus("COMPLETED");
        } else if (status == OrderStatus.CANCELLED) {
            order.setPaymentStatus("CANCELLED");
            // Restock products on cancellation
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        Order updated = orderRepository.save(order);
        return mapToDto(updated);
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomPart = new Random().nextInt(9000) + 1000;
        return "JDM" + datePart + randomPart;
    }

    public OrderDto mapToDto(Order o) {
        OrderDto dto = new OrderDto();
        dto.setId(o.getId());
        dto.setOrderNumber(o.getOrderNumber());
        if (o.getUser() != null) {
            dto.setUserId(o.getUser().getId());
            dto.setUserEmail(o.getUser().getEmail());
            dto.setUserFullName(o.getUser().getFullName());
        }
        dto.setShippingFullName(o.getShippingFullName());
        dto.setShippingMobile(o.getShippingMobile());
        dto.setShippingAddress(String.format("%s, %s, %s, %s - %s",
                o.getShippingHouseBuilding(), o.getShippingStreet(),
                o.getShippingCity(), o.getShippingState(), o.getShippingPincode()));
        dto.setPaymentMethod(o.getPaymentMethod());
        dto.setPaymentStatus(o.getPaymentStatus());
        dto.setStatus(o.getStatus());
        dto.setSubtotal(o.getSubtotal());
        dto.setDeliveryCharge(o.getDeliveryCharge());
        dto.setDiscount(o.getDiscount());
        dto.setTotalAmount(o.getTotalAmount());
        dto.setCreatedAt(o.getCreatedAt());

        List<OrderItemDto> itemDtos = o.getItems().stream().map(i -> new OrderItemDto(
                i.getId(),
                i.getProduct() != null ? i.getProduct().getId() : null,
                i.getProductName(),
                i.getProductImage(),
                i.getPrice(),
                i.getQuantity(),
                i.getSubtotal()
        )).collect(Collectors.toList());
        dto.setItems(itemDtos);

        return dto;
    }
}
