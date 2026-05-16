package edu.orders.server.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private String id;
    private String description;
    private BigDecimal price;
    private BigDecimal quantity;
}
