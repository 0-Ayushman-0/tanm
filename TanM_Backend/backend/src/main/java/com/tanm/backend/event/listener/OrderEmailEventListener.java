package com.tanm.backend.event.listener;

import com.tanm.backend.event.OrderPaidEvent;
import com.tanm.backend.event.OrderShippedEvent;
import com.tanm.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEmailEventListener {

    private final EmailService emailService;

    @EventListener
    public void handleOrderPaid(OrderPaidEvent event) {
        log.info("🔔 OrderPaidEvent received for order: {}", event.getOrderDto().getOrderNumber());
        emailService.sendOrderConfirmation(event.getOrderDto());
    }

    @EventListener
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("🔔 OrderShippedEvent received for order: {}", event.getOrderDto().getOrderNumber());
        emailService.sendShippingUpdate(event.getOrderDto());
    }
}
