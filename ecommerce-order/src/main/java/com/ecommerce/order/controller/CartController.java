package com.ecommerce.order.controller;

import com.ecommerce.common.core.result.Result;
import com.ecommerce.common.web.annotation.RequireLogin;
import com.ecommerce.common.web.context.UserContextHolder;
import com.ecommerce.order.dto.CartAddRequest;
import com.ecommerce.order.dto.CartItemDTO;
import com.ecommerce.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @RequireLogin
    @PostMapping("/add")
    public Result<Void> addToCart(@RequestBody @Valid CartAddRequest request) {
        cartService.addToCart(UserContextHolder.getUserId(), request);
        return Result.success();
    }

    @RequireLogin
    @GetMapping("/list")
    public Result<List<CartItemDTO>> listCartItems() {
        return Result.success(cartService.listCartItems(UserContextHolder.getUserId()));
    }

    @RequireLogin
    @PutMapping("/{cartItemId}/quantity")
    public Result<Void> updateQuantity(@PathVariable Long cartItemId,
                                       @RequestParam Integer quantity) {
        cartService.updateQuantity(UserContextHolder.getUserId(), cartItemId, quantity);
        return Result.success();
    }

    @RequireLogin
    @DeleteMapping("/{cartItemId}")
    public Result<Void> removeCartItem(@PathVariable Long cartItemId) {
        cartService.removeCartItem(UserContextHolder.getUserId(), cartItemId);
        return Result.success();
    }

    @RequireLogin
    @DeleteMapping("/clear")
    public Result<Void> clearCart() {
        cartService.clearCart(UserContextHolder.getUserId());
        return Result.success();
    }
}
