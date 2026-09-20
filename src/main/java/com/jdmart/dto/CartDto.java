package com.jdmart.dto;

import java.util.ArrayList;
import java.util.List;

public class CartDto {

    private Long id;
    private List<CartItemDto> items = new ArrayList<>();
    private Integer totalQuantity = 0;
    private Double subtotal = 0.0;
    private Double originalTotal = 0.0;
    private Double discount = 0.0;
    private Double deliveryCharge = 0.0;
    private Double totalAmount = 0.0;

    public CartDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getOriginalTotal() {
        return originalTotal;
    }

    public void setOriginalTotal(Double originalTotal) {
        this.originalTotal = originalTotal;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(Double deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
