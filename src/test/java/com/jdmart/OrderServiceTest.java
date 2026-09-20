package com.jdmart;

import com.jdmart.dto.CartItemRequest;
import com.jdmart.dto.CheckoutRequest;
import com.jdmart.dto.OrderDto;
import com.jdmart.dto.ProductDto;
import com.jdmart.model.OrderStatus;
import com.jdmart.model.User;
import com.jdmart.repository.UserRepository;
import com.jdmart.security.CustomUserDetails;
import com.jdmart.service.CartService;
import com.jdmart.service.OrderService;
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
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUpSecurityContext() {
        User user = userRepository.findByEmail("priya.patel@example.com").orElseThrow();
        CustomUserDetails userDetails = CustomUserDetails.build(user);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testPlaceOrder_Success() {
        List<ProductDto> products = productService.getAllProducts();
        ProductDto product = products.get(0);

        // Add to cart
        cartService.addToCart(new CartItemRequest(product.getId(), 1));

        // Checkout
        CheckoutRequest checkout = new CheckoutRequest();
        checkout.setFullName("Priya Patel");
        checkout.setMobile("9898765432");
        checkout.setHouseBuilding("B-12, Green Avenue");
        checkout.setStreet("Satellite Road");
        checkout.setCity("Ahmedabad");
        checkout.setState("Gujarat");
        checkout.setPincode("380015");
        checkout.setPaymentMethod("Cash on Delivery");

        OrderDto order = orderService.placeOrder(checkout);

        assertNotNull(order);
        assertNotNull(order.getOrderNumber());
        assertTrue(order.getOrderNumber().startsWith("JDM"));
        assertEquals(OrderStatus.ORDERED, order.getStatus());
        assertEquals(1, order.getItems().size());
        assertTrue(order.getTotalAmount() > 0);
    }
}
