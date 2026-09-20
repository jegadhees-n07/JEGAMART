package com.jdmart;

import com.jdmart.dto.ProductDto;
import com.jdmart.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Test
    void testGetAllProducts() {
        List<ProductDto> products = productService.getAllProducts();
        assertNotNull(products);
        assertTrue(products.size() >= 30, "Should have at least 30 seeded demo products");
    }

    @Test
    void testSearchProducts() {
        List<ProductDto> searchSamsung = productService.searchProducts("Samsung");
        assertNotNull(searchSamsung);
        assertFalse(searchSamsung.isEmpty());
        assertTrue(searchSamsung.stream().anyMatch(p -> p.getName().contains("Samsung")));
    }

    @Test
    void testFeaturedProducts() {
        List<ProductDto> featured = productService.getFeaturedProducts();
        assertNotNull(featured);
        assertFalse(featured.isEmpty());
        assertTrue(featured.stream().allMatch(ProductDto::isFeatured));
    }
}
