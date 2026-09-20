package com.jdmart.service;

import com.jdmart.dto.CartDto;
import com.jdmart.dto.CartItemDto;
import com.jdmart.dto.CartItemRequest;
import com.jdmart.exception.BadRequestException;
import com.jdmart.exception.ResourceNotFoundException;
import com.jdmart.model.Cart;
import com.jdmart.model.CartItem;
import com.jdmart.model.Product;
import com.jdmart.model.User;
import com.jdmart.repository.CartItemRepository;
import com.jdmart.repository.CartRepository;
import com.jdmart.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AuthService authService;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       AuthService authService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.authService = authService;
    }

    @Transactional
    public Cart getOrCreateCartForUser(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    @Transactional(readOnly = true)
    public CartDto getCart() {
        User user = authService.getAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);
        return mapToCartDto(cart);
    }

    @Transactional
    public CartDto addToCart(CartItemRequest request) {
        User user = authService.getAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!product.isActive()) {
            throw new BadRequestException("Product is currently unavailable");
        }

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Only " + product.getStockQuantity() + " items available in stock");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new BadRequestException("Cannot add more: only " + product.getStockQuantity() + " items available");
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(cart, product, request.getQuantity());
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }

        Cart updatedCart = cartRepository.findById(cart.getId()).orElse(cart);
        return mapToCartDto(updatedCart);
    }

    @Transactional
    public CartDto updateCartItem(Long itemId, int quantity) {
        User user = authService.getAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Unauthorized access to cart item");
        }

        if (quantity <= 0) {
            cart.removeItem(item);
            cartItemRepository.delete(item);
        } else {
            if (item.getProduct().getStockQuantity() < quantity) {
                throw new BadRequestException("Only " + item.getProduct().getStockQuantity() + " items available in stock");
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        Cart updatedCart = cartRepository.findById(cart.getId()).orElse(cart);
        return mapToCartDto(updatedCart);
    }

    @Transactional
    public CartDto removeCartItem(Long itemId) {
        return updateCartItem(itemId, 0);
    }

    @Transactional
    public void clearCart() {
        User user = authService.getAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public CartDto mapToCartDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());

        double subtotal = 0.0;
        double originalTotal = 0.0;
        int totalQuantity = 0;

        for (CartItem item : cart.getItems()) {
            Product p = item.getProduct();
            double itemSubtotal = Math.round(p.getSellingPrice() * item.getQuantity() * 100.0) / 100.0;
            double itemOrigTotal = Math.round(p.getOriginalPrice() * item.getQuantity() * 100.0) / 100.0;

            subtotal += itemSubtotal;
            originalTotal += itemOrigTotal;
            totalQuantity += item.getQuantity();

            CartItemDto itemDto = new CartItemDto(
                    item.getId(),
                    p.getId(),
                    p.getName(),
                    p.getImageUrl(),
                    p.getSellingPrice(),
                    p.getOriginalPrice(),
                    p.getDiscountPercentage(),
                    item.getQuantity(),
                    p.getStockQuantity(),
                    itemSubtotal
            );
            dto.getItems().add(itemDto);
        }

        subtotal = Math.round(subtotal * 100.0) / 100.0;
        originalTotal = Math.round(originalTotal * 100.0) / 100.0;
        double discount = Math.round((originalTotal - subtotal) * 100.0) / 100.0;

        // Delivery charge rule: Free if subtotal >= 500, else ₹40 (free if 0 items)
        double deliveryCharge = (subtotal >= 500.0 || totalQuantity == 0) ? 0.0 : 40.0;
        double totalAmount = Math.round((subtotal + deliveryCharge) * 100.0) / 100.0;

        dto.setTotalQuantity(totalQuantity);
        dto.setSubtotal(subtotal);
        dto.setOriginalTotal(originalTotal);
        dto.setDiscount(discount);
        dto.setDeliveryCharge(deliveryCharge);
        dto.setTotalAmount(totalAmount);

        return dto;
    }
}
