package edu.orders.server.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.orders.server.data.model.OrderStatus;
import edu.orders.server.dto.ItemDto;
import edu.orders.server.dto.OrderCallBackDto;
import edu.orders.server.dto.OrderRequestDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StubsFactory {

    public static OrderRequestDto getDemoOrderRequestDto(String callbackUrl) {
        List<ItemDto> items = new ArrayList<>();
        items.add(ItemDto.builder().id("test-item-id-AAA").description("test-item-descAAA").price(new BigDecimal("123.45")).quantity(BigDecimal.TEN).build());
        items.add(ItemDto.builder().id("test-item-id-BBB").description("test-item-desc-BBB").price(new BigDecimal("321.54")).quantity(BigDecimal.ONE).build());
        return OrderRequestDto.builder()
                .requestId("test-requestId")
                .items(items)
                .callbackUrl(callbackUrl)
                .build();
    }

    public static OrderCallBackDto getOrderCallBackDto(String customerId, String orderId) {
        List<ItemDto> items = new ArrayList<>();
        items.add(ItemDto.builder().id("test-item-id-AAA").description("test-item-descAAA").price(new BigDecimal("123.45")).quantity(BigDecimal.TEN).build());
        items.add(ItemDto.builder().id("test-item-id-BBB").description("test-item-desc-BBB").price(new BigDecimal("321.54")).quantity(BigDecimal.ONE).build());
        return OrderCallBackDto.builder()
                .customerId(customerId)
                .orderId(orderId)
                .items(items)
                .status(OrderStatus.PROCESSED)
                .build();
    }

    public static HttpHeaders prepareAuthorisationHeaders(String password, String customerId) {
        HttpHeaders headers = new HttpHeaders();
        String formattedDate = getFormattedDate();
        headers.set("C-Base-Date", formattedDate);
        headers.set("C-Base-IV", "generateIvString");
        headers.set("C-Customer-Id", customerId);
        headers.set("C-Base-Auth", Base64.getEncoder().encodeToString(password.getBytes()));
        return headers;
    }

    public static String getFormattedDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss"));
    }

    public static ObjectMapper getDummyObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Allow empty strings to be treated as null for object types
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        return objectMapper;
    }
}
