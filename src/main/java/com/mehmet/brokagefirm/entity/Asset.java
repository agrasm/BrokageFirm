package com.mehmet.brokagefirm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity

@IdClass(AssetId.class)
public class Asset {
    @Id
    private Long customerId;
    @Id
    private String assetName;

    private BigDecimal size;
    private BigDecimal usableSize;
}