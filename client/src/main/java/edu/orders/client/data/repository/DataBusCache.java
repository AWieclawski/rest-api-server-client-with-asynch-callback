package edu.orders.client.data.repository;

import edu.orders.client.data.model.ApiError;
import edu.orders.client.data.model.Customer;
import edu.orders.client.data.model.Item;
import edu.orders.client.data.model.Order;
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

    private volatile Map<String, ApiError> apiErrorMap;

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

    Map<String, ApiError> getApiErrorMap() {
        if (this.apiErrorMap == null) {
            this.apiErrorMap = new ConcurrentHashMap<>();
        }
        return this.apiErrorMap;
    }

    Map<String, Customer> getCustomersMap() {
        if (this.customersMap == null) {
            this.customersMap = new ConcurrentHashMap<>();
            this.customersMap.put("defaultOrderClientId", Customer.builder().customerId("defaultOrderClientId").password("defaultOrderClientPassword").build());
        }
        return this.customersMap;
    }

}
