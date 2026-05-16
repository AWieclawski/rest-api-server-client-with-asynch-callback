package edu.orders.server.data.mapper;

import edu.orders.server.data.model.Item;
import edu.orders.server.dto.ItemDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemsMapper {

    public static Item toEntity(ItemDto dto, String orderId) {
        return dto != null ? Item.builder()
                .id(dto.getId())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .description(dto.getDescription())
                .orderId(orderId)
                .build()
                : null;
    }

    public static ItemDto toDto(Item entity) {
        return entity != null ? ItemDto.builder()
                .id(entity.getId())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
                .description(entity.getDescription())
                .build()
                : null;
    }

    public static List<ItemDto> toDtoList(List<Item> entityList) {
        return entityList != null ? entityList.stream().map(ItemsMapper::toDto).collect(Collectors.toList()) : new ArrayList<>();
    }

    public static List<Item> toEntityList(List<ItemDto> dtoList, String orderId) {
        return dtoList != null ? dtoList.stream().map(it -> toEntity(it, orderId)).collect(Collectors.toList()) : new ArrayList<>();
    }
}
