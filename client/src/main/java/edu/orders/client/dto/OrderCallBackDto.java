package edu.orders.client.dto;

import edu.orders.client.data.model.OrderStatus;
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
    private OrderStatus status;
    private List<ItemDto> items;
}
