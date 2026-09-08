package com.ecommerce.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountDTO implements Serializable {

    private int total;
    private int system;
    private int order;
    private int promotion;
    private int aftersales;
}
