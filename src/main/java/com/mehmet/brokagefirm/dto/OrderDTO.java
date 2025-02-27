package com.mehmet.brokagefirm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDTO {
    private Long customerId;
    private String assetName;
    private String orderSide;
    private Long size;
    private Long price;
}