package edu.orders.server.data.service;

import edu.orders.server.data.mapper.ItemsMapper;
import edu.orders.server.data.mapper.OrdersMapper;
import edu.orders.server.data.model.Item;
import edu.orders.server.data.model.Order;
import edu.orders.server.data.model.OrderStatus;
import edu.orders.server.data.repository.ItemsRepository;
import edu.orders.server.data.repository.OrdersRepository;
import edu.orders.server.dto.OrderCallBackDto;
import edu.orders.server.dto.OrderRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;

@Service(OrderService.BEAN_NAME)
@RequiredArgsConstructor
@Slf4j
@DependsOn(OrdersRepository.BEAN_NAME)
public class OrderService {

    public static final String BEAN_NAME = "edu.orders.server.data.service.OrderService";

    private final ItemsRepository itemsRepository;
    private final OrdersRepository ordersRepository;


    public Order saveOrder(final OrderCallBackDto orderCallBackDto) {
        Order result;
        try {
            result = ordersRepository.insertOrder(OrdersMapper.toEntity(orderCallBackDto));
            log.info("Order [{}] saved successfully", result);
            List<Item> items = itemsRepository.insertItemList(ItemsMapper.toEntityList(orderCallBackDto.getItems(), orderCallBackDto.getOrderId()));
            log.info("Items [{}] saved successfully", items);
        } catch (Exception ex) {
            log.error("Order saving failed! class: {} message: {} ", ex.getClass(), ex.getMessage(), ex.getCause());
            throw new RuntimeException("Order persistence process error!");
        }
        return result;
    }

    public List<Item> findByOrderId(String orderId) {
        return itemsRepository.findByOrderId(orderId);
    }

    public OrderCallBackDto buildOrderCallBackDto(OrderRequestDto requestDto, final String orderId) {
        return OrderCallBackDto.builder()
                .orderId(orderId)
                .customerId(requestDto.getCustomerId())
                .items(requestDto.getItems())
                .status(OrderStatus.PROCESSED)
                .build();

    }
}
