package edu.orders.client.controller;

import edu.orders.client.crypto.CryptoClientConverter;
import edu.orders.client.data.service.ApiErrorService;
import edu.orders.client.data.service.OrderService;
import edu.orders.client.dto.ApiErrorDto;
import edu.orders.client.dto.OrderCallBackDto;
import edu.orders.client.dto.OrderHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CryptoClientConverter cryptoClientConverter;
    private final OrderService orderService;
    private final ApiErrorService apiErrorService;

    @PostMapping("/confirmed")
    public ResponseEntity<Void> createOrder(HttpServletRequest requestEntity) {
        OrderCallBackDto requestBodyDto = null;
        String requestId = requestEntity.getHeader(OrderHeader.REQUEST_ID.getValue());
        String errorResponse = requestEntity.getHeader(OrderHeader.ERROR_RESPONSE.getValue());
        boolean orderVerification = false;

        // handle if error response
        if (errorResponse != null) {
            try {
                ApiErrorDto apiErrorDto = cryptoClientConverter.convertToApiErrorDto(requestEntity);
                apiErrorService.saveError(apiErrorDto);
                // Return 202 Accepted with request ID in header
                log.warn("Request not confirmed. {}", requestId);
                return ResponseEntity.accepted()
                        .header(OrderHeader.REQUEST_ID.getValue(), requestId)
                        .build();
            } catch (Exception ex) {
                log.error("Error confirmation from Server failed! class: {} message: {} ", ex.getClass(), ex.getMessage(), ex.getCause());
            }
        }

        try {
            requestBodyDto = cryptoClientConverter.convertToOrderCallBackDto(requestEntity);
            orderVerification = orderService.orderVerification(requestBodyDto);
        } catch (Exception ex) {
            log.error("Order confirmation from Server failed! class: {} message: {} ", ex.getClass(), ex.getMessage(), ex.getCause());
        }

        // handle if response DTO OK
        if (requestBodyDto != null && orderVerification) {
            log.info("Order [{}] confirmed. Request [{}] completed", requestBodyDto, requestId);
            // Return 202 Accepted with order ID in header
            return ResponseEntity.accepted()
                    .header(OrderHeader.ORDER_ID.getValue(), requestBodyDto.getOrderId())
                    .header(OrderHeader.REQUEST_ID.getValue(), requestId)
                    .build();
        } else {
            log.warn("Order not confirmed. Order Verification {}", orderVerification);
            // Return 400 Bad Request
            return ResponseEntity.badRequest()
                    .header(OrderHeader.ORDER_ID.getValue(), "n/a")
                    .header(OrderHeader.REQUEST_ID.getValue(), requestId)
                    .build();
        }

    }

}
