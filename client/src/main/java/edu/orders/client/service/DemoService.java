package edu.orders.client.service;

import edu.orders.client.data.mapper.ItemsMapper;
import edu.orders.client.data.mapper.OrdersMapper;
import edu.orders.client.data.service.OrderService;
import edu.orders.client.dto.ItemDto;
import edu.orders.client.dto.OrderRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@DependsOn(OrderRequestService.BEAN_NAME)
public class DemoService {

    private final OrderRequestService asyncService;
    private final OrderService orderService;

    @Value("${order-client.demo}")
    private boolean demoEnabled;

    @PostConstruct
    public void initDemo() {
        if (demoEnabled) {
            try {
                doDemoRequest();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void doDemoRequest() {
        Instant start = Instant.now();
        OrderRequestDto orderRequestDto = getDemoOrderRequestDto();
        CompletableFuture<String> completableFuture = asyncService.sendOrder(orderRequestDto);
        try {
            String orderId = completableFuture.get();
            log.debug("Order Server Response body: {}", completableFuture.get());
            orderService.saveOrder(OrdersMapper.toEntity(orderRequestDto, orderId),
                    ItemsMapper.toEntityList(orderRequestDto.getItems(), orderId));
        } catch (Exception ex) {
            log.error("Demo Request error! class: {} message: {}", ex.getClass(), ex.getMessage(), ex.getCause());
        }

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        log.info("Total time: {} ms", timeElapsed);
    }

    private OrderRequestDto getDemoOrderRequestDto() {
        List<ItemDto> items = new ArrayList<>();
        items.add(ItemDto.builder().id("test-item-id-AAA").description("test-item-descAAA").price(new BigDecimal("123.45")).quantity(BigDecimal.TEN).build());
        items.add(ItemDto.builder().id("test-item-id-BBB").description("test-item-desc-BBB").price(new BigDecimal("321.54")).quantity(BigDecimal.ONE).build());
        return OrderRequestDto.builder()
                .requestId("test-requestId")
                .items(items)
                .build();
    }

}
