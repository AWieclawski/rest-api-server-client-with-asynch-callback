package edu.orders.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.orders.server.crypto.CryptoServerConverter;
import edu.orders.server.dto.ItemDto;
import edu.orders.server.dto.OrderHeader;
import edu.orders.server.dto.OrderRequestDto;
import edu.orders.server.service.BaseDateService;
import edu.orders.server.service.OrderResponseService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = OrderController.class)
@ActiveProfiles("test")
class OrderControllerInitialResponseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${endpoint.orders}")
    private String orderEndPoint;

    @Value("${endpoint.order-create}")
    private String methodEndPoint;

    @MockBean
    private OrderResponseService mockOrderResponseService;

    @MockBean
    private CryptoServerConverter mockCryptoServerConverter;

    @MockBean
    private BaseDateService baseDateService;

    @Test
    void createOrder_ShouldReturn202Accepted_WithOrderIdHeader() throws Exception {
        // 1. Define the request payload (callback URL can be dummy for initial response test)
        String url = "http://localhost:8888/api/orders/confirmed";
        String requestBody = objectMapper.writeValueAsString(getDemoOrderRequestDto(url));

        Mockito.when(baseDateService.getBaseTimestampId()).thenReturn("2026061109237654321");
        Mockito.when(mockOrderResponseService.getCryptoServerConverter()).thenReturn(mockCryptoServerConverter);
        Mockito.when(mockCryptoServerConverter.convertRequestBody(any(HttpServletRequest.class))).thenReturn(getDemoOrderRequestDto(url));
        Mockito.when(mockOrderResponseService.processOrderAsync(any(OrderRequestDto.class), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        // 2. Send POST request to /orders
        mockMvc.perform(post(orderEndPoint + methodEndPoint)
                        .headers(prepareAuthorisationHeaders("password", "customerId"))
                        .contentType("text/plain;charset=ISO-8859-1")
                        .accept("text/plain, application/json, application/*+json, */*")
                        .content(requestBody))

                // 3. Validate response
                .andExpect(status().isAccepted()) // 202 Accepted
                .andExpect(header().exists(OrderHeader.ORDER_ID.getValue())); // Order ID header exists
    }

    private OrderRequestDto getDemoOrderRequestDto(String url) {
        List<ItemDto> items = new ArrayList<>();
        items.add(ItemDto.builder().id("test-item-id-AAA").description("test-item-descAAA").price(new BigDecimal("123.45")).quantity(BigDecimal.TEN).build());
        items.add(ItemDto.builder().id("test-item-id-BBB").description("test-item-desc-BBB").price(new BigDecimal("321.54")).quantity(BigDecimal.ONE).build());
        return OrderRequestDto.builder()
                .requestId("test-requestId")
                .items(items)
                .callbackUrl(url)
                .build();
    }

    HttpHeaders prepareAuthorisationHeaders(String password, String customerId) {
        HttpHeaders headers = new HttpHeaders();
        String formattedDate = getFormattedDate();
        headers.set("C-Base-Date", formattedDate);
        headers.set("C-Base-IV", "generateIvString");
        headers.set("C-Customer-Id", customerId);
        headers.set("C-Base-Auth", Base64.getEncoder().encodeToString(password.getBytes()));
        return headers;
    }

    private String getFormattedDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss"));
    }
}