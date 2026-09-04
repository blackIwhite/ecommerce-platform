package com.ecommerce.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ecommerce.api.product.ProductApi;
import com.ecommerce.api.product.dto.SkuDTO;
import com.ecommerce.order.dto.CartAddRequest;
import com.ecommerce.order.dto.CartItemDTO;
import com.ecommerce.order.entity.CartItem;
import com.ecommerce.order.mapper.CartItemMapper;
import com.ecommerce.order.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductApi productApi;

    @Override
    @Transactional
    public void addToCart(Long userId, CartAddRequest request) {
        CartItem existing = cartItemMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getSkuId, request.getSkuId())
        );

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            cartItemMapper.updateById(existing);
        } else {
            CartItem item = CartItem.builder()
                    .userId(userId)
                    .skuId(request.getSkuId())
                    .quantity(request.getQuantity())
                    .build();
            cartItemMapper.insert(item);
        }
    }

    @Override
    public List<CartItemDTO> listCartItems(Long userId) {
        List<CartItem> cartItems = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .orderByDesc(CartItem::getCreateTime)
        );

        if (cartItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> skuIds = cartItems.stream().map(CartItem::getSkuId).toList();
        List<SkuDTO> skuList = productApi.getSkuListByIds(skuIds).getData();
        Map<Long, SkuDTO> skuMap = skuList.stream()
                .collect(Collectors.toMap(SkuDTO::getSkuId, Function.identity()));

        return cartItems.stream().map(item -> {
            SkuDTO sku = skuMap.get(item.getSkuId());
            return CartItemDTO.builder()
                    .id(item.getId())
                    .skuId(item.getSkuId())
                    .skuName(sku != null ? sku.getSkuName() : "")
                    .price(sku != null ? sku.getPrice() : null)
                    .stock(sku != null ? sku.getStock() : 0)
                    .image(sku != null ? sku.getImage() : "")
                    .specs(sku != null ? sku.getSpecs() : "")
                    .quantity(item.getQuantity())
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Cart item not found");
        }
        item.setQuantity(quantity);
        cartItemMapper.updateById(item);
    }

    @Override
    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Cart item not found");
        }
        cartItemMapper.deleteById(cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartItemMapper.delete(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId)
        );
    }
}
