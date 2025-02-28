package com.mehmet.brokagefirm.service;

import com.mehmet.brokagefirm.dto.OrderDTO;
import com.mehmet.brokagefirm.entity.Asset;
import com.mehmet.brokagefirm.entity.Customer;
import com.mehmet.brokagefirm.entity.Order;
import com.mehmet.brokagefirm.enums.OrderSide;
import com.mehmet.brokagefirm.enums.OrderStatus;
import com.mehmet.brokagefirm.handler.BrokageLogicException;
import com.mehmet.brokagefirm.repository.CustomerRepository;
import com.mehmet.brokagefirm.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class OrderServiceTest {
    @Mock
    private LoginService loginService;

    @Mock
    private AssetService assetService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderService orderService;

    private OrderDTO order;

    private OrderDTO orderTry;

    private OrderDTO orderZeroSize;

    private OrderDTO orderZeroPrice;

    private Order savedOrder;

    private Asset asset;

    private Asset assetZero;

    private Asset assetTry;

    private Asset assetTryZero;

    private User adminUser;

    private User standardUser;

    private Customer customer;

    private void initialize(String side) {
        order = new OrderDTO();
        order.setCustomerId(1L);
        order.setAssetName("ING");
        order.setOrderSide(side);
        order.setSize(BigDecimal.TEN);
        order.setPrice(BigDecimal.ONE);

        orderZeroSize = new OrderDTO();
        orderZeroSize.setCustomerId(1L);
        orderZeroSize.setAssetName("ING");
        orderZeroSize.setOrderSide(side);
        orderZeroSize.setSize(BigDecimal.ZERO);
        orderZeroSize.setPrice(BigDecimal.ONE);

        orderZeroPrice = new OrderDTO();
        orderZeroPrice.setCustomerId(1L);
        orderZeroPrice.setAssetName("ING");
        orderZeroPrice.setOrderSide(side);
        orderZeroPrice.setSize(BigDecimal.TEN);
        orderZeroPrice.setPrice(BigDecimal.ZERO);

        orderTry = new OrderDTO();
        orderTry.setCustomerId(1L);
        orderTry.setAssetName("TRY");
        orderTry.setOrderSide(side);
        orderTry.setSize(BigDecimal.TEN);
        orderTry.setPrice(BigDecimal.ONE);

        savedOrder = new Order();
        savedOrder.setCustomerId(1L);
        savedOrder.setAssetName("ING");
        savedOrder.setOrderSide("BUY");
        savedOrder.setSize(BigDecimal.TEN);
        savedOrder.setPrice(BigDecimal.ONE);
        savedOrder.setStatus(OrderStatus.PENDING.name());

        asset = new Asset();
        asset.setAssetName("ING");
        asset.setSize(BigDecimal.valueOf(100));
        asset.setUsableSize(BigDecimal.valueOf(100));
        asset.setCustomerId(1L);

        assetTry = new Asset();
        assetTry.setAssetName("TRY");
        assetTry.setSize(BigDecimal.valueOf(100));
        assetTry.setUsableSize(BigDecimal.valueOf(100));
        assetTry.setCustomerId(1L);

        assetZero = new Asset();
        assetZero.setAssetName("ING");
        assetZero.setSize(BigDecimal.ZERO);
        assetZero.setUsableSize(BigDecimal.ZERO);
        assetZero.setCustomerId(1L);

        assetTryZero = new Asset();
        assetTryZero.setAssetName("TRY");
        assetTryZero.setSize(BigDecimal.ZERO);
        assetTryZero.setUsableSize(BigDecimal.ZERO);
        assetTryZero.setCustomerId(1L);

        customer = new Customer();
        customer.setId(2L);
        customer.setName("customer2");
        customer.setPassword("1234");
        customer.setRole("USER");


        adminUser = (User) User.withDefaultPasswordEncoder()
                .username("admin")
                .password("1234")
                .roles("ADMIN")
                .build();

        standardUser = (User) User.withDefaultPasswordEncoder()
                .username("customer1")
                .password("1234")
                .roles("USER")
                .build();
    }

    @Test
    void testListOrders() {
        initialize(OrderSide.BUY.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(asset);
        when(orderRepository.findByCustomerIdAndCreateDateBetween(any(), any(), any())).thenReturn(List.of(savedOrder));
        List<Order> orders = orderService.listOrders(1L, "2024-01-01", "2025-12-12");
        Assertions.assertNotNull(orders);
        Assertions.assertEquals(1L, orders.get(0).getCustomerId());
    }

    @Test
    void testListOrdersThrowsExceptionWhenDateFormatIsIncorrect() {
        initialize(OrderSide.BUY.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(asset);
        when(orderRepository.findByCustomerIdAndCreateDateBetween(any(), any(), any())).thenReturn(List.of(savedOrder));
        Exception exception = Assertions.assertThrows(BrokageLogicException.class, () -> orderService.listOrders(1L, "20240101", "20251212"));

        Assertions.assertEquals("Incorrect date format, correct format is: yyyy-MM-dd", exception.getMessage());
    }


    @Test
    void testCreateOrderForBuy() {
        initialize(OrderSide.BUY.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(asset);
        when(orderRepository.save(any())).thenReturn(savedOrder);
        Order createdOrder = orderService.createOrder(order);
        Assertions.assertNotNull(createdOrder);
        Assertions.assertEquals(OrderStatus.PENDING.name(), createdOrder.getStatus());
    }

    @Test
    void testCreateOrderForSell() {
        initialize(OrderSide.SELL.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(asset);
        when(orderRepository.save(any())).thenReturn(savedOrder);
        Order createdOrder = orderService.createOrder(order);
        Assertions.assertNotNull(createdOrder);
        Assertions.assertEquals(OrderStatus.PENDING.name(), createdOrder.getStatus());
    }

    @Test
    void testCreateOrderIsNotAllowedForDifferentCustomerIfRoleIsNotAdmin() {
        initialize(OrderSide.SELL.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn("USER");
        Mockito.when(loginService.getCurrentUser()).thenReturn(standardUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(asset);
        when(customerRepository.findCustomerByName(any())).thenReturn(customer);
        Exception exception = Assertions.assertThrows(BrokageLogicException.class, () -> orderService.createOrder(order));

        Assertions.assertEquals("User can create order for itself only", exception.getMessage());

    }

    @Test
    void testCreateOrderIsNotAllowedForTryAsset() {
        initialize(OrderSide.SELL.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(assetTry);
        when(orderRepository.save(any())).thenReturn(savedOrder);
        Exception exception = Assertions.assertThrows(BrokageLogicException.class, () -> orderService.createOrder(orderTry));

        Assertions.assertEquals("TRY is not allowed", exception.getMessage());

    }

    @Test
    void testCreateOrderIsNotAllowedForZeroSize() {
        initialize(OrderSide.SELL.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(assetTry);
        when(orderRepository.save(any())).thenReturn(savedOrder);
        Exception exception = Assertions.assertThrows(BrokageLogicException.class, () -> orderService.createOrder(orderZeroSize));

        Assertions.assertEquals("Incorrect order size or price", exception.getMessage());

    }

    @Test
    void testCreateOrderIsNotAllowedForZeroPrice() {
        initialize(OrderSide.SELL.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(assetTry);
        when(orderRepository.save(any())).thenReturn(savedOrder);
        Exception exception = Assertions.assertThrows(BrokageLogicException.class, () -> orderService.createOrder(orderZeroPrice));

        Assertions.assertEquals("Incorrect order size or price", exception.getMessage());

    }

    @Test
    void testCreateOrderIfOrderTypeIsIncorrect() {
        initialize("WRONG_SIDE");
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(asset);
        when(orderRepository.save(any())).thenReturn(savedOrder);
        Exception exception = Assertions.assertThrows(BrokageLogicException.class, () -> orderService.createOrder(order));

        Assertions.assertEquals("Incorrect Order Type", exception.getMessage());

    }

    @Test
    void testCreateOrderBuyForNotEnoughTry() {
        initialize(OrderSide.BUY.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(assetTryZero);
        when(orderRepository.save(any())).thenReturn(savedOrder);
        Exception exception = Assertions.assertThrows(BrokageLogicException.class, () -> orderService.createOrder(order));

        Assertions.assertEquals("Not enough TRY for this order", exception.getMessage());

    }

    @Test
    void testCreateOrderSellNotEnoughAsset() {
        initialize(OrderSide.SELL.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(assetZero);
        when(orderRepository.save(any())).thenReturn(savedOrder);
        Exception exception = Assertions.assertThrows(BrokageLogicException.class, () -> orderService.createOrder(order));

        Assertions.assertEquals("Not enough usable size for this order", exception.getMessage());

    }

    @Test
    void testCreateOrderSellAssetDoesNotExist() {
        initialize(OrderSide.SELL.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(null);
        when(orderRepository.save(any())).thenReturn(savedOrder);
        Exception exception = Assertions.assertThrows(BrokageLogicException.class, () -> orderService.createOrder(order));

        Assertions.assertEquals("Asset does not exist", exception.getMessage());

    }

    @Test
    void testDeleteOrder() {
        initialize(OrderSide.SELL.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(asset);
        when(orderRepository.findById(any())).thenReturn(Optional.of(savedOrder));
        Order deletedOrder = orderService.deleteOrder(1L);
        Assertions.assertNotNull(deletedOrder);
        Assertions.assertEquals(OrderStatus.CANCELED.name(), deletedOrder.getStatus());
    }

    @Test
    void testMatchOrderForBuy() {
        initialize(OrderSide.BUY.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(asset);
        when(orderRepository.findById(any())).thenReturn(Optional.of(savedOrder));
        Order matchOrder = orderService.match(1L);
        Assertions.assertNotNull(matchOrder);
        Assertions.assertEquals(OrderStatus.MATCHED.name(), matchOrder.getStatus());
    }

    @Test
    void testMatchOrderForSell() {
        initialize(OrderSide.SELL.name());
        Mockito.when(loginService.getCurrentUserRole()).thenReturn(LoginService.ADMIN);
        Mockito.when(loginService.getCurrentUser()).thenReturn(adminUser);
        when(assetService.findByCustomerIdAndAssetName(any(), any())).thenReturn(asset);
        when(orderRepository.findById(any())).thenReturn(Optional.of(savedOrder));
        Order matchOrder = orderService.match(1L);
        Assertions.assertNotNull(matchOrder);
        Assertions.assertEquals(OrderStatus.MATCHED.name(), matchOrder.getStatus());
    }
}
