package edu.orders.client.data.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class Item {
    private String id;
    private String description;
    private BigDecimal price;
    private BigDecimal quantity;
    private String orderId;
}
