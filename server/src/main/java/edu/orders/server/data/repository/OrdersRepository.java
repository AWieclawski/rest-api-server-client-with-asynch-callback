package edu.orders.server.data.repository;

import edu.orders.server.data.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

@Repository(OrdersRepository.BEAN_NAME)
@RequiredArgsConstructor
@Slf4j
@DependsOn(DataBusCache.BEAN_NAME)
public class OrdersRepository {

    public static final String BEAN_NAME = "edu.orders.server.data.repository.OrdersRepository";

    private final DataBusCache dataBusCache;

    public Order findByOrderId(String orderId) {
        return dataBusCache.getOrdersMap().get(orderId);
    }

    public Order insertOrder(Order order) {
        dataBusCache.getOrdersMap().put(order.getOrderId(), order);
        return dataBusCache.getOrdersMap().get(order.getOrderId());
    }
}
