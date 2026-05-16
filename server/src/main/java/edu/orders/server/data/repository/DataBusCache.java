package edu.orders.server.data.repository;

import edu.orders.server.data.model.Customer;
import edu.orders.server.data.model.Item;
import edu.orders.server.data.model.Order;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(DataBusCache.BEAN_NAME)
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class DataBusCache {

    public static final String BEAN_NAME = "edu.orders.server.data.repository.DataBusCache";

    private volatile Map<String, Order> ordersMap;

    private volatile Map<String, Item> itemsMap;

    private volatile Map<String, Customer> customersMap;

    Map<String, Order> getOrdersMap() {
        if (this.ordersMap == null) {
            this.ordersMap = new ConcurrentHashMap<>();
        }
        return this.ordersMap;
    }

    Map<String, Item> getItemsMap() {
        if (this.itemsMap == null) {
            this.itemsMap = new ConcurrentHashMap<>();
        }
        return this.itemsMap;
    }

    Map<String, Customer> getCustomersMap() {
        if (this.customersMap == null) {
            this.customersMap = new ConcurrentHashMap<>();
            this.customersMap.put("defaultOrderClientId", Customer.builder().customerId("defaultOrderClientId").password("defaultOrderClientPassword").build());
        }
        return this.customersMap;
    }

}
