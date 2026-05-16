package edu.orders.server.data.model;

import lombok.*;

@Getter
@Builder
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String customerId;
    @Setter
    private OrderStatus status;
}
