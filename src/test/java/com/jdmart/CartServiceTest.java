package com.jdmart;

import com.jdmart.dto.CartDto;
import com.jdmart.dto.CartItemRequest;
import com.jdmart.dto.ProductDto;
import com.jdmart.model.User;
import com.jdmart.repository.UserRepository;
import com.jdmart.security.CustomUserDetails;
import com.jdmart.service.CartService;
import com.jdmart.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUpSecurityContext() {
        User user = userRepository.findByEmail("rahul.sharma@example.com").orElseThrow();
        CustomUserDetails userDetails = CustomUserDetails.build(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testAddToCartAndCalculateTotals() {
        List<ProductDto> products = productService.getAllProducts();
        assertFalse(products.isEmpty());
        ProductDto product = products.get(0);

        CartItemRequest request = new CartItemRequest(product.getId(), 2);
        CartDto cart = cartService.addToCart(request);

        assertNotNull(cart);
        assertFalse(cart.getItems().isEmpty());
        assertEquals(2, cart.getTotalQuantity());
        assertTrue(cart.getTotalAmount() > 0);

        // Add 1 more of same product -> quantity should become 3
        CartItemRequest requestMore = new CartItemRequest(product.getId(), 1);
        CartDto updatedCart = cartService.addToCart(requestMore);
        assertEquals(3, updatedCart.getTotalQuantity());
    }
}
