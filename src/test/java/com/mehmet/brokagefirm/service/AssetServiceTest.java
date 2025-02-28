package com.mehmet.brokagefirm.service;

import com.mehmet.brokagefirm.entity.Asset;
import com.mehmet.brokagefirm.repository.AssetRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;

@SpringBootTest
class AssetServiceTest {
    @Mock
    private LoginService loginService;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    private Asset asset;

    private Asset assetTry;

    private Asset assetDifferentCustomer;

    private User user;

    private void initialize() {
        assetTry = new Asset();
        assetTry.setAssetName("TRY");
        assetTry.setSize(BigDecimal.valueOf(100));
        assetTry.setUsableSize(BigDecimal.valueOf(100));
        assetTry.setCustomerId(1L);

        asset = new Asset();
        asset.setAssetName("ING");
        asset.setSize(BigDecimal.valueOf(100));
        asset.setUsableSize(BigDecimal.valueOf(100));
        asset.setCustomerId(1L);

        assetDifferentCustomer = new Asset();
        assetDifferentCustomer.setAssetName("ING");
        assetDifferentCustomer.setSize(BigDecimal.valueOf(100));
        assetDifferentCustomer.setUsableSize(BigDecimal.valueOf(100));
        assetDifferentCustomer.setCustomerId(2L);


        user = (User) User.withDefaultPasswordEncoder()
                .username("admin")
                .password("1234")
                .roles("ADMIN")
                .build();
    }

    @Test
    void testListAllAssets() {
        initialize();
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(user);
        when(assetRepository.findAll()).thenReturn(List.of(asset, assetTry));
        List<Asset> assets = assetService.listAssets();
        Assertions.assertNotNull(assets);
        Assertions.assertEquals(1L, assets.get(0).getCustomerId());
        Assertions.assertEquals(1L, assets.get(1).getCustomerId());
    }

    @Test
    void testListAssetsByCustomerId() {
        initialize();
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(user);
        when(assetRepository.findByCustomerId(2L)).thenReturn(List.of(assetDifferentCustomer));
        List<Asset> assets = assetService.listAssetsByCustomerId(2L);
        Assertions.assertNotNull(assets);
        Assertions.assertEquals(2L, assets.get(0).getCustomerId());
    }

}