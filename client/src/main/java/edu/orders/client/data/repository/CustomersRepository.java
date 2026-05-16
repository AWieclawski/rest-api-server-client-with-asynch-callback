package edu.orders.client.data.repository;

import edu.orders.client.data.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository(CustomersRepository.BEAN_NAME)
@RequiredArgsConstructor
@DependsOn(DataBusCache.BEAN_NAME)
public class CustomersRepository {

    public static final String BEAN_NAME = "edu.orders.client.data.repository.CustomersRepository";

    private final DataBusCache dataBusCache;

    public Customer findByCustomerId(String customerId) {
        return dataBusCache.getCustomersMap().get(customerId);
    }

}
