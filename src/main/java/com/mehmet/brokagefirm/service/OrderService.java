package com.mehmet.brokagefirm.service;

import com.mehmet.brokagefirm.dto.OrderDTO;
import com.mehmet.brokagefirm.entity.Asset;
import com.mehmet.brokagefirm.entity.Order;
import com.mehmet.brokagefirm.enums.OrderSide;
import com.mehmet.brokagefirm.enums.OrderStatus;
import com.mehmet.brokagefirm.handler.BrokageLogicException;
import com.mehmet.brokagefirm.repository.CustomerRepository;
import com.mehmet.brokagefirm.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final AssetService assetService;
    private final LoginService loginService;
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TRY = "TRY";

    public Order createOrder(OrderDTO orderRequested) {

        if (!LoginService.ADMIN.equals(loginService.getCurrentUserRole()) && !orderRequested.getCustomerId().equals(customerRepository.findCustomerByName(loginService.getCurrentUser().getUsername()).getId())) {
            throw new BrokageLogicException("User can create order for itself only");
        }

        if (TRY.equals(orderRequested.getAssetName())) {
            throw new BrokageLogicException("TRY is not allowed");
        }
        if (OrderSide.BUY.name().equals(orderRequested.getOrderSide())) {
            return buyOrder(orderRequested);
        } else if (OrderSide.SELL.name().equals(orderRequested.getOrderSide())) {
            return sellOrder(orderRequested);
        } else {
            log.error("Incorrect Order Type: {}", orderRequested.getOrderSide());
            throw new BrokageLogicException("Incorrect Order Type");
        }
    }

    public Order prepareOrder(OrderDTO orderRequested) {
        Order order = new Order();
        order.setCustomerId(orderRequested.getCustomerId());
        order.setAssetName(orderRequested.getAssetName());
        order.setSize(orderRequested.getSize());
        order.setPrice(orderRequested.getPrice());
        order.setOrderSide(orderRequested.getOrderSide());
        order.setStatus(OrderStatus.PENDING.name());
        order.setCreateDate(LocalDateTime.now());

        return orderRepository.save(order);
    }

    private Order buyOrder(OrderDTO orderRequested) {
        Long orderTotalPrice = orderRequested.getPrice() * orderRequested.getSize();
        Asset tryAsset = assetService.findByCustomerIdAndAssetName(orderRequested.getCustomerId(), TRY);
        if (tryAsset.getUsableSize() < orderTotalPrice) {
            log.error("Not enough TRY for this order: {}", orderTotalPrice);
            throw new BrokageLogicException("Not enough TRY for this order");
        } else {
            tryAsset.setUsableSize(tryAsset.getUsableSize() - orderTotalPrice);
            assetService.updateAsset(tryAsset);
            return prepareOrder(orderRequested);
        }
    }

    private Order sellOrder(OrderDTO orderRequested) {
        Asset currentAsset = assetService.findByCustomerIdAndAssetName(orderRequested.getCustomerId(), orderRequested.getAssetName());
        if (ObjectUtils.isEmpty(currentAsset)) {
            log.error("Asset does not exist: {}", orderRequested.getAssetName());
            throw new BrokageLogicException("Asset does not exist");
        }
        if (currentAsset.getUsableSize() < orderRequested.getSize()) {
            log.error("Not enough usable size for this order: {}", orderRequested.getSize());
            throw new BrokageLogicException("Not enough usable size for this order");
        } else {
            currentAsset.setUsableSize(currentAsset.getUsableSize() - orderRequested.getSize());
            assetService.updateAsset(currentAsset);
            return prepareOrder(orderRequested);
        }

    }

    public List<Order> listOrders(Long customerId, String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        LocalDateTime startTime;
        LocalDateTime endTime;
        if (!LoginService.ADMIN.equals(loginService.getCurrentUserRole()) && !customerId.equals(customerRepository.findCustomerByName(loginService.getCurrentUser().getUsername()).getId())) {
            throw new BrokageLogicException("User can list order for itself only");
        }
        try {
            startTime = LocalDate.parse(startDate, formatter).atStartOfDay();
            endTime = LocalDate.parse(endDate, formatter).atStartOfDay();
        } catch (Exception e) {
            log.error("Incorrect date format: {}", e.getMessage());
            throw new BrokageLogicException("Incorrect date format, correct format is: yyyy-MM-dd");
        }
        return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startTime, endTime);
    }

    public Order deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BrokageLogicException("Order not found"));
        if (!LoginService.ADMIN.equals(loginService.getCurrentUserRole()) && !order.getCustomerId().equals(customerRepository.findCustomerByName(loginService.getCurrentUser().getUsername()).getId())) {
            throw new BrokageLogicException("User can delete order for itself only");
        }
        if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
            throw new BrokageLogicException("Only PENDING orders can be canceled");
        }
        if (OrderSide.BUY.name().equals(order.getOrderSide())) {
            cancelBuyOrder(order);
        } else if (OrderSide.SELL.name().equals(order.getOrderSide())) {
            cancelSellOrder(order);
        }
        order.setStatus(OrderStatus.CANCELED.name());
        orderRepository.save(order);
        return order;
    }

    private void cancelBuyOrder(Order orderRequested) {
        Long orderTotalPrice = orderRequested.getPrice() * orderRequested.getSize();
        Asset tryAsset = assetService.findByCustomerIdAndAssetName(orderRequested.getCustomerId(), TRY);
        tryAsset.setUsableSize(tryAsset.getUsableSize() + orderTotalPrice);
        assetService.updateAsset(tryAsset);

    }

    private void cancelSellOrder(Order orderRequested) {
        Asset currentAsset = assetService.findByCustomerIdAndAssetName(orderRequested.getCustomerId(), orderRequested.getAssetName());
        currentAsset.setUsableSize(currentAsset.getUsableSize() + orderRequested.getSize());
        assetService.updateAsset(currentAsset);
    }

    public Order match(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new BrokageLogicException("Order not found"));
        if (!LoginService.ADMIN.equals(loginService.getCurrentUserRole())) {
            throw new BrokageLogicException("Admin users can match orders only");
        }
        if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
            throw new BrokageLogicException("Only PENDING orders can be matched");
        }
        if (OrderSide.BUY.name().equals(order.getOrderSide())) {
            matchBuyOrder(order);
        } else if (OrderSide.SELL.name().equals(order.getOrderSide())) {
            matchSellOrder(order);
        }
        order.setStatus(OrderStatus.MATCHED.name());
        orderRepository.save(order);
        return order;
    }

    private void matchBuyOrder(Order orderRequested) {
        Long orderTotalPrice = orderRequested.getPrice() * orderRequested.getSize();
        Asset tryAsset = assetService.findByCustomerIdAndAssetName(orderRequested.getCustomerId(), TRY);
        tryAsset.setSize(tryAsset.getSize() - orderTotalPrice);
        assetService.updateAsset(tryAsset);
        Asset currentAsset = assetService.findByCustomerIdAndAssetName(orderRequested.getCustomerId(), orderRequested.getAssetName());
        if (ObjectUtils.isEmpty(currentAsset)) {
            Asset newAsset = new Asset();
            newAsset.setCustomerId(orderRequested.getCustomerId());
            newAsset.setAssetName(orderRequested.getAssetName());
            newAsset.setSize(orderRequested.getSize());
            newAsset.setUsableSize(orderRequested.getSize());
            assetService.updateAsset(newAsset);
        } else {
            currentAsset.setSize(currentAsset.getSize() + orderRequested.getSize());
            currentAsset.setUsableSize(currentAsset.getUsableSize() + orderRequested.getSize());
            assetService.updateAsset(currentAsset);
        }

    }

    private void matchSellOrder(Order orderRequested) {
        Long orderTotalPrice = orderRequested.getPrice() * orderRequested.getSize();
        Asset currentAsset = assetService.findByCustomerIdAndAssetName(orderRequested.getCustomerId(), orderRequested.getAssetName());
        currentAsset.setSize(currentAsset.getSize() - orderRequested.getSize());
        assetService.updateAsset(currentAsset);
        Asset tryAsset = assetService.findByCustomerIdAndAssetName(orderRequested.getCustomerId(), TRY);
        tryAsset.setUsableSize(tryAsset.getUsableSize() + orderTotalPrice);
        tryAsset.setSize(tryAsset.getSize() + orderTotalPrice);
    }


}