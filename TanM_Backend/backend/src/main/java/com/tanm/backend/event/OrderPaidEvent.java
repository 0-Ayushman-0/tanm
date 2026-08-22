package com.tanm.backend.event;

import com.tanm.backend.dto.OrderDto;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderPaidEvent extends ApplicationEvent {
    private final OrderDto orderDto;

    public OrderPaidEvent(Object source, OrderDto orderDto) {
        super(source);
        this.orderDto = orderDto;
    }
}
