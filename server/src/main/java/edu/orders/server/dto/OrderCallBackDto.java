package edu.orders.server.dto;

import edu.orders.server.data.model.OrderStatus;
import lombok.*;

import java.util.List;

@Getter
@Builder
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class OrderCallBackDto {
    private String orderId;
    private String customerId;
    @Setter
    private OrderStatus status;
    private List<ItemDto> items;
}
