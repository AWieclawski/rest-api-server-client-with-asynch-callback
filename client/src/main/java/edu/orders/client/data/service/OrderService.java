package edu.orders.client.data.service;

import edu.orders.client.data.mapper.ItemsMapper;
import edu.orders.client.data.mapper.OrdersMapper;
import edu.orders.client.data.model.Item;
import edu.orders.client.data.model.Order;
import edu.orders.client.data.repository.ItemsRepository;
import edu.orders.client.data.repository.OrdersRepository;
import edu.orders.client.dto.ItemDto;
import edu.orders.client.dto.OrderCallBackDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service(OrderService.BEAN_NAME)
@RequiredArgsConstructor
@Slf4j
@DependsOn(OrdersRepository.BEAN_NAME)
public class OrderService {

    public static final String BEAN_NAME = "edu.orders.client.data.service.OrderService";

    private final ItemsRepository itemsRepository;
    private final OrdersRepository ordersRepository;

    public Order saveOrder(final OrderCallBackDto orderCallBackDto) {
        return saveOrder(OrdersMapper.toEntity(orderCallBackDto),
                ItemsMapper.toEntityList(orderCallBackDto.getItems(), orderCallBackDto.getOrderId()));
    }

    public Order saveOrder(final Order order, List<Item> items) {
        Order result = null;
        try {
            result = ordersRepository.insertOrder(order);
            log.info("Order [{}] saved successfully", result);
            List<Item> itemsSaved = itemsRepository.insertItemList(items);
            log.info("Items [{}] saved successfully", itemsSaved);
        } catch (Exception e) {
            log.error("Order persistence process error! {}", e.getMessage(), e);
        }
        return result;
    }

    public boolean orderVerification(OrderCallBackDto requestBodyDto) {
        Order order = findById(requestBodyDto.getOrderId());
        List<Item> items = getItemsByOrderId(requestBodyDto.getOrderId());
        return compareOrders(order, items, requestBodyDto);
    }

    public Order findById(String orderId) {
        Order result = null;
        try {
            result = ordersRepository.findByOrderId(orderId);
        } catch (Exception e) {
            log.error("Order {} not found! {}", orderId, e.getMessage(), e);
        }
        return result;
    }

    public boolean compareOrders(Order oldOrder, List<Item> olderItems, OrderCallBackDto newOrderCallBackDto) {
        boolean result = true;
        try {
            assert Objects.equals(oldOrder.getOrderId(), newOrderCallBackDto.getOrderId());
            List<ItemDto> newDtoItems = newOrderCallBackDto.getItems();
            olderItems.forEach(oldItem -> {
                newDtoItems.stream().filter(newItem -> Objects.equals(newItem.getId(), oldItem.getId())).findFirst()
                        .ifPresent(newItem -> {
                            assert Objects.equals(0, oldItem.getPrice().compareTo(newItem.getPrice()));
                            assert Objects.equals(0, oldItem.getQuantity().compareTo(newItem.getQuantity()));
                            assert Objects.equals(oldItem.getDescription(), newItem.getDescription());
                        });

            });
        } catch (Exception e) {
            log.error("Order comparison failed! {}", oldOrder.getOrderId());
            result = false;
        }
        return result;
    }

    public List<Item> getItemsByOrderId(String orderId) {
        return itemsRepository.findByOrderId(orderId);
    }
}
