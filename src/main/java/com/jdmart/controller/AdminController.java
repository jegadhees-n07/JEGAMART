package com.jdmart.controller;

import com.jdmart.dto.*;
import com.jdmart.model.User;
import com.jdmart.repository.UserRepository;
import com.jdmart.service.DashboardService;
import com.jdmart.service.OrderService;
import com.jdmart.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DashboardService dashboardService;
    private final ProductService productService;
    private final OrderService orderService;
    private final UserRepository userRepository;

    public AdminController(DashboardService dashboardService,
                           ProductService productService,
                           OrderService orderService,
                           UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.productService = productService;
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics", stats));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts() {
        List<ProductDto> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.success("All products retrieved", products));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@Valid @RequestBody ProductDto dto) {
        ProductDto created = productService.createProduct(dto);
        return new ResponseEntity<>(ApiResponse.success("Product created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDto dto) {
        ProductDto updated = productService.updateProduct(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully"));
    }

    @PatchMapping("/products/{id}/stock")
    public ResponseEntity<ApiResponse<ProductDto>> updateStock(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        Integer stock = payload.get("stockQuantity");
        if (stock == null) stock = 0;
        ProductDto updated = productService.updateStock(id, stock);
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", updated));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<OrderDto> orders = orderService.getAllOrders(page, size);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", orders));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderDto updated = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", updated));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserProfileDto>>> getAllUsers() {
        List<UserProfileDto> users = userRepository.findAll().stream().map(u -> new UserProfileDto(
                u.getId(),
                u.getFullName(),
                u.getEmail(),
                u.getMobile(),
                u.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toList()),
                null
        )).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }
}
