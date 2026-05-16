package edu.orders.client.service;

import edu.orders.client.crypto.CryptoClientConverter;
import edu.orders.client.dto.OrderHeader;
import edu.orders.client.dto.OrderRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service(OrderRequestService.BEAN_NAME)
@RequiredArgsConstructor
@DependsOn(CryptoClientConverter.BEAN_NAME)
public class OrderRequestService {

    public final static String BEAN_NAME = "edu.orders.client.service.OrderRequestService";
    private final RestTemplate restTemplate;
    private final CryptoClientConverter cryptoClientConverter;

    @Value("${remote.order.server}")
    private String urlRemoteOrderServer;

    @Async
    public CompletableFuture<String> sendOrder(OrderRequestDto orderRequestDto) {
        final String url = urlRemoteOrderServer;
        ResponseEntity<String> responseEntity = null;

        // Create a Request Entity
        HttpEntity<String> requestEntity = cryptoClientConverter.getOrderCustomerRequest(orderRequestDto);

        // Get Response Entity
        try {
            responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            log.error("Rest template exchange with Server failed! {} ", e.getMessage(), e);
        }

        // Show Response Entity Headers
        try {
            assert responseEntity != null;
            HttpHeaders headers = responseEntity.getHeaders();
            log.debug("Order [{}] sent and Request [{}] confirmed",
                    headers.getFirst(OrderHeader.ORDER_ID.getValue()),
                    headers.getFirst(OrderHeader.REQUEST_ID.getValue())
            );
            return CompletableFuture.completedFuture(headers.getFirst(OrderHeader.ORDER_ID.getValue()));
        } catch (Throwable e) {
            log.error("Order Server Response failed! {}", e.getMessage(), e);
        }

        return CompletableFuture.completedFuture("Send Order error");
    }

}
