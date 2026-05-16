package edu.orders.client.data.mapper;

import edu.orders.client.data.model.Item;
import edu.orders.client.dto.ItemDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
