package com.jdmart.controller;

import com.jdmart.dto.ApiResponse;
import com.jdmart.dto.ProductDto;
import com.jdmart.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false, defaultValue = "default") String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            List<ProductDto> results = productService.searchProducts(keyword);
            return ResponseEntity.ok(ApiResponse.success("Search results", results));
        }

        if (categoryId != null || minPrice != null || maxPrice != null || minRating != null || !sort.equals("default")) {
            Page<ProductDto> filtered = productService.filterProducts(categoryId, minPrice, maxPrice, minRating, sort, page, size);
            return ResponseEntity.ok(ApiResponse.success("Filtered products", filtered));
        }

        Page<ProductDto> products = productService.getProductsPaged(page, size, "id", "asc");
        return ResponseEntity.ok(ApiResponse.success("Products retrieved", products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product details", product));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDto>>> searchProducts(@RequestParam String keyword) {
        List<ProductDto> results = productService.searchProducts(keyword);
        return ResponseEntity.ok(ApiResponse.success("Search results for '" + keyword + "'", results));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductDto> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Products for category", products));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getFeaturedProducts() {
        List<ProductDto> featured = productService.getFeaturedProducts();
        return ResponseEntity.ok(ApiResponse.success("Featured products", featured));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getTrendingProducts() {
        List<ProductDto> trending = productService.getTrendingProducts();
        return ResponseEntity.ok(ApiResponse.success("Trending products", trending));
    }
}
