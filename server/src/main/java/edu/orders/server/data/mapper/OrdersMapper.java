package edu.orders.server.data.mapper;

import edu.orders.server.data.model.Item;
import edu.orders.server.data.model.Order;
import edu.orders.server.dto.OrderCallBackDto;

import java.util.List;

public class OrdersMapper {

    public static Order toEntity(OrderCallBackDto dto) {
        return dto != null ? Order.builder()
                .orderId(dto.getOrderId())
                .customerId(dto.getCustomerId())
                .status(dto.getStatus())
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
