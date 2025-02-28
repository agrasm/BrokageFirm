package com.mehmet.brokagefirm.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderDTO {
    private Long customerId;
    private String assetName;
    private String orderSide;
    private BigDecimal size;
    private BigDecimal price;
}