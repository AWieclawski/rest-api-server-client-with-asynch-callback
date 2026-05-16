package edu.orders.client.data.mapper;

import edu.orders.client.data.model.Item;
import edu.orders.client.data.model.Order;
import edu.orders.client.data.model.OrderStatus;
import edu.orders.client.dto.OrderCallBackDto;
import edu.orders.client.dto.OrderRequestDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrdersMapper {

    public static Order toEntity(OrderCallBackDto dto) {
        return dto != null ? Order.builder()
                .orderId(dto.getOrderId())
                .customerId(dto.getCustomerId())
                .status(dto.getStatus())
                .build()
                : null;
    }

    public static Order toEntity(OrderRequestDto dto, String orderId) {
        return dto != null ? Order.builder()
                .orderId(orderId)
                .customerId(dto.getCustomerId())
                .status(OrderStatus.NEW)
                .build()
                : null;
    }

    public static OrderCallBackDto toDto(Order entity, List<Item> itemList) {
        return entity != null ? OrderCallBackDto.builder()
                .orderId(entity.getOrderId())
                .customerId(entity.getCustomerId())
                .items(ItemsMapper.toDtoList(itemList))
                .build()
                : null;
    }
}
