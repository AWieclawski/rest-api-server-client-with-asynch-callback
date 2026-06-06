package edu.orders.server.controller;

import edu.orders.server.dto.ApiErrorDto;
import edu.orders.server.dto.OrderHeader;
import edu.orders.server.dto.OrderRequestDto;
import edu.orders.server.service.BaseDateService;
import edu.orders.server.service.OrderResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("${endpoint.orders}")
@RequiredArgsConstructor
public class OrderController {

    private final OrderResponseService orderResponseService;

    private final BaseDateService baseDateService;

    @PostMapping("${endpoint.order-create}")
    public ResponseEntity<Object> createOrder(HttpServletRequest requestEntity) {

        // Generate a unique order ID
        String orderId = baseDateService.getBaseTimestampId();

        // Convert headers to OrderRequestDto
        OrderRequestDto request;
        try {
            request = orderResponseService.getCryptoServerConverter().convertRequestBody(requestEntity);
        } catch (Exception ex) {
            log.error("Request entity error. class {} message: {} ", ex.getClass(), ex.getMessage(), ex.getCause());
            ApiErrorDto requestError = ErrorHelper.requestErrorBuild(ex, requestEntity);

            // Return 400 Bad Request with order ID in header
            return ResponseEntity.badRequest()
                    .header(OrderHeader.REQUEST_ID.getValue(), orderId)
                    .body(requestError);
        }

        // Trigger async processing (does NOT block the response)
        orderResponseService.processOrderAsync(request, orderId);

        // Return 202 Accepted with order ID in header
        return ResponseEntity.accepted()
                .header(OrderHeader.ORDER_ID.getValue(), orderId)
                .header(OrderHeader.REQUEST_ID.getValue(), request.getRequestId())
                .build();

    }
}
