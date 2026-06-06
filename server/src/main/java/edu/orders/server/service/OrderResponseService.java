package edu.orders.server.service;

import edu.orders.server.controller.ErrorHelper;
import edu.orders.server.crypto.CryptoServerConverter;
import edu.orders.server.data.mapper.OrdersMapper;
import edu.orders.server.data.model.Item;
import edu.orders.server.data.model.Order;
import edu.orders.server.data.model.OrderStatus;
import edu.orders.server.data.service.OrderService;
import edu.orders.server.dto.ApiErrorDto;
import edu.orders.server.dto.OrderCallBackDto;
import edu.orders.server.dto.OrderRequestDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderResponseService {

    public final static String BEAN_NAME = "edu.orders.client.service.OrderRequestService";

    private final RestTemplate restTemplate;
    @Getter
    private final CryptoServerConverter cryptoServerConverter;
    private final OrderService orderService;

    // Mark method as async (runs in a background thread)
    @Async
    public CompletableFuture<Void> processOrderAsync(final OrderRequestDto requestDto, final String orderId) {
        return CompletableFuture.runAsync(() -> {

            // Create callback payload
            OrderCallBackDto orderCallBackDto = orderService.buildOrderCallBackDto(requestDto, orderId);

            try {
                // processing in another Thread
                Thread processOrderThread = getProcessOrderThread(requestDto, orderCallBackDto);
                processOrderThread.start();
            } catch (Exception e) {

                // Create failed order callback payload
                orderCallBackDto.setStatus(OrderStatus.FAILED);
                restTemplate.postForObject(requestDto.getCallbackUrl(), orderCallBackDto, Void.class);
            }
        });
    }

    private Thread getProcessOrderThread(final OrderRequestDto orderRequestDto, final OrderCallBackDto orderCallBackDto) {
        return new Thread(() -> {
            Order result;
            try {
                // Simulate persistence process (e.g., 1 second)
                result = orderService.saveOrder(orderCallBackDto);
                doSomethingMore();
            } catch (Exception ex) {
                log.error("Order saving thread failed! class: {} message: {} ", ex.getClass(), ex.getMessage(), ex.getCause());
                sendApiErrorDto(ex, orderRequestDto);
                Thread.currentThread().interrupt();
                throw new RuntimeException("Order persistence thread error! ");
            }
            List<Item> itemList = orderService.findByOrderId(result.getOrderId());
            sendCallbackOrder(orderRequestDto, OrdersMapper.toDto(result, itemList));
        });
    }

    private void doSomethingMore() throws InterruptedException {
        Thread.sleep(1000);
//        throw new RuntimeException("TEST error 001");
    }

    private void sendApiErrorDto(Throwable ex, final OrderRequestDto orderRequestDto) {
        ApiErrorDto requestError = ErrorHelper.getApiErrorDto(ex, orderRequestDto.getRequestId());
        try {
            HttpEntity<String> responseEntity = cryptoServerConverter.getErrorServerResponse(orderRequestDto, requestError);
            restTemplate.exchange(orderRequestDto.getCallbackUrl(), HttpMethod.POST, responseEntity, String.class);
        } catch (Exception exx) {
            Thread.currentThread().interrupt();
            log.error("Order send Error Response! class: {} message: {} ", exx.getClass(), exx.getMessage(), exx.getCause());
        }
    }

    // Send callback to client's URL
    private void sendCallbackOrder(OrderRequestDto orderRequestDto, OrderCallBackDto orderCallBackDto) {
        HttpEntity<String> orderServerResponse =
                cryptoServerConverter.getCallBackServerResponse(orderCallBackDto, orderRequestDto.getRequestId());

        // Show Response Entity Headers
        try {
            ResponseEntity<String> responseEntity =
                    restTemplate.exchange(orderRequestDto.getCallbackUrl(), HttpMethod.POST, orderServerResponse, String.class);
            HttpHeaders headers = responseEntity.getHeaders();
            log.debug("Client Response Entity confirmed {}", headers);
        } catch (Throwable e) {
            log.error("Client Order confirmation Response failed! {}", e.getMessage(), e);
        }
    }

}
