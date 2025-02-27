package com.mehmet.brokagefirm.controller;

import com.mehmet.brokagefirm.entity.Asset;
import com.mehmet.brokagefirm.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;

    @GetMapping("/{customerId}")
    public List<Asset> listAssetsByCustomerId(@PathVariable Long customerId) {
        return assetService.listAssetsByCustomerId(customerId);
    }

    @GetMapping("/all")
    public List<Asset> listAssets() {
        return assetService.listAssets();
    }
}