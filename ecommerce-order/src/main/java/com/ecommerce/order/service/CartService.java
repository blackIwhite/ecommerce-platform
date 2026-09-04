package com.ecommerce.order.service;

import com.ecommerce.order.dto.CartAddRequest;
import com.ecommerce.order.dto.CartItemDTO;

import java.util.List;

public interface CartService {

    void addToCart(Long userId, CartAddRequest request);

    List<CartItemDTO> listCartItems(Long userId);

    void updateQuantity(Long userId, Long cartItemId, Integer quantity);

    void removeCartItem(Long userId, Long cartItemId);

    void clearCart(Long userId);
}
