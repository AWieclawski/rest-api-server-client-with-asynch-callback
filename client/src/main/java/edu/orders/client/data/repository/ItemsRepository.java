package edu.orders.client.data.repository;

import edu.orders.client.data.model.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository(ItemsRepository.BEAN_NAME)
@RequiredArgsConstructor
@Slf4j
@DependsOn(DataBusCache.BEAN_NAME)
public class ItemsRepository {

    public static final String BEAN_NAME = "edu.orders.client.data.repository.ItemsRepository";

    private final DataBusCache dataBusCache;

    public List<Item> findByOrderId(String orderId) {
        return dataBusCache.getItemsMap().values().parallelStream()
                .filter(it -> Objects.equals(it.getOrderId(), orderId)).collect(Collectors.toList());
    }

    public Item findByItemId(String itemId) {
        return dataBusCache.getItemsMap().get(itemId);
    }

    public Item insertItem(Item item) {
        dataBusCache.getItemsMap().put(item.getId(), item);
        return dataBusCache.getItemsMap().get(item.getId());
    }
    public List<Item> insertItemList(List<Item> items) {
        return items.stream().map(this::insertItem).collect(Collectors.toList());
    }

}
