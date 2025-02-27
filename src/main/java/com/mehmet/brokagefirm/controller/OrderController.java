package com.mehmet.brokagefirm.controller;

import com.mehmet.brokagefirm.dto.OrderDTO;
import com.mehmet.brokagefirm.entity.Order;
import com.mehmet.brokagefirm.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public Order createOrder(@RequestBody OrderDTO order) {
        return orderService.createOrder(order);
    }

    @GetMapping("/list/{customerId}/{startDate}/{endDate}")
    public List<Order> listOrders(@PathVariable Long customerId, @PathVariable String startDate, @PathVariable String endDate) {
        return orderService.listOrders(customerId, startDate, endDate);
    }

    @DeleteMapping("/cancel/{orderId}")
    public Order cancelOrder(@PathVariable Long orderId) {
        return orderService.deleteOrder(orderId);
    }

    @PostMapping("/match/{orderId}")
    public Order matchOrder(@PathVariable Long orderId) {
        return orderService.match(orderId);
    }
}