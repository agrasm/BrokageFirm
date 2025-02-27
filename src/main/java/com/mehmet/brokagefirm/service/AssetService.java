package com.mehmet.brokagefirm.service;

import com.mehmet.brokagefirm.entity.Asset;
import com.mehmet.brokagefirm.handler.BrokageLogicException;
import com.mehmet.brokagefirm.repository.AssetRepository;
import com.mehmet.brokagefirm.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;
    private final CustomerRepository customerRepository;
    private final LoginService loginService;

    public List<Asset> listAssets() {
        if (LoginService.ADMIN.equals(loginService.getCurrentUserRole())) {
            return assetRepository.findAll();
        }
        return assetRepository.findByCustomerId(customerRepository.findCustomerByName(loginService.getCurrentUser().getUsername()).getId());
    }

    public List<Asset> listAssetsByCustomerId(Long customerId) {
        if (!LoginService.ADMIN.equals(loginService.getCurrentUserRole()) && !customerId.equals(customerRepository.findCustomerByName(loginService.getCurrentUser().getUsername()).getId())) {
            throw new BrokageLogicException("User can list assets for itself only");
        }
        return assetRepository.findByCustomerId(customerId);
    }

    public Asset findByCustomerIdAndAssetName(Long customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetName(customerId, assetName);
    }

    public Asset updateAsset(Asset asset) {
        return assetRepository.save(asset);
    }
}
