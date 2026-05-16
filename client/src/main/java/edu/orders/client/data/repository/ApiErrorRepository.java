package edu.orders.client.data.repository;

import edu.orders.client.data.model.ApiError;
import edu.orders.client.data.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

@Repository(ApiErrorRepository.BEAN_NAME)
@RequiredArgsConstructor
@Slf4j
@DependsOn(DataBusCache.BEAN_NAME)
public class ApiErrorRepository {

    public static final String BEAN_NAME = "edu.orders.client.data.repository.ApiErrorRepository";

    private final DataBusCache dataBusCache;

    public ApiError insertError(ApiError apiError) {
        dataBusCache.getApiErrorMap().put(apiError.getErrorId(), apiError);
        return dataBusCache.getApiErrorMap().get(apiError.getErrorId());
    }
}
