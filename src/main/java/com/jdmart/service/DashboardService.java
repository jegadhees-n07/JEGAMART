package com.jdmart.service;

import com.jdmart.dto.DashboardStatsDto;
import com.jdmart.model.OrderStatus;
import com.jdmart.repository.OrderRepository;
import com.jdmart.repository.ProductRepository;
import com.jdmart.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public DashboardService(UserRepository userRepository, ProductRepository productRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        Double totalSales = orderRepository.calculateTotalSales();
        if (totalSales == null) totalSales = 0.0;

        long activeOrders = orderRepository.countByStatus(OrderStatus.ORDERED)
                + orderRepository.countByStatus(OrderStatus.CONFIRMED)
                + orderRepository.countByStatus(OrderStatus.SHIPPED)
                + orderRepository.countByStatus(OrderStatus.OUT_FOR_DELIVERY);

        long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);

        return new DashboardStatsDto(
                totalUsers,
                totalProducts,
                totalOrders,
                Math.round(totalSales * 100.0) / 100.0,
                activeOrders,
                deliveredOrders
        );
    }
}
