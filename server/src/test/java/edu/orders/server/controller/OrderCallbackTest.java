package edu.orders.server.controller;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.orders.server.crypto.CryptoServerConverter;
import edu.orders.server.data.model.Customer;
import edu.orders.server.data.model.OrderStatus;
import edu.orders.server.data.repository.CustomersRepository;
import edu.orders.server.dto.ItemDto;
import edu.orders.server.dto.OrderCallBackDto;
import edu.orders.server.dto.OrderHeader;
import edu.orders.server.dto.OrderRequestDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * <a href="https://www.baeldung.com/spring-mocking-webclient">lore</a>
 */

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc  // Inject MockMvc
@ActiveProfiles("test")
class OrderCallbackTest {

    @Autowired
    private MockMvc mockMvc;

    private MockWebServer mockCallbackServer; // Mock client's callback endpoint

    @Value("${endpoint.orders}")
    private String orderEndPoint;

    @Value("${endpoint.order-create}")
    private String methodEndPoint;

    @MockBean
    private CustomersRepository mockCustomersRepository;

    @MockBean
    private CryptoServerConverter mockCryptoServerConverter;

    @BeforeEach
    void setUp() throws Exception {
        // Start the mock server on a random port
        mockCallbackServer = new MockWebServer();
        mockCallbackServer.start();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s",
                mockCallbackServer.getPort());
        log.debug("Mock server initialised with baseUrl: {}", baseUrl);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Stop the mock server after tests
        mockCallbackServer.shutdown();
    }


    @Test
    void processOrderAsync_ShouldSendCallback_WithCorrectPayload() throws Exception {
        String orderId = "orderId";
        String orderRequestId = "orderRequestId";
        String customerId = "customerId";
        String customerPass = "customerPass";
        Customer customer = Customer.builder().customerId(customerId).password(customerPass).build();
        Mockito.when(mockCustomersRepository.findByCustomerId(anyString())).thenReturn(customer);

        String url = "/api/orders/confirmed";
        // 1. Get the mock callback server's URL (e.g., "http://localhost:54321/callback")
        String callbackUrl = mockCallbackServer.url(url).toString();
        log.debug("Mock server callback url confirmed: {}", callbackUrl);
        OrderRequestDto demoOrderRequestDto = getDemoOrderRequestDto(callbackUrl);
        OrderCallBackDto orderCallBackDto = getOrderCallBackDto(customerId, orderId);
        String encodedBody = getDummyObjectMapper().writeValueAsString(orderCallBackDto);
        Mockito.when(mockCryptoServerConverter.convertRequestBody(any(HttpServletRequest.class))).thenReturn(demoOrderRequestDto);
        Mockito.when(mockCryptoServerConverter.getCallBackServerResponse(any(OrderCallBackDto.class), anyString()))
                .thenReturn(new HttpEntity<>(encodedBody, prepareAuthorisationHeaders(customerPass, customerPass)));
        // MockWebServer will respond with the queued stub.
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "text/plain;charset=ISO-8859-1")
                .addHeader(OrderHeader.REQUEST_ID.getValue(), orderRequestId);
        mockCallbackServer.enqueue(mockResponse);

        // 2. Define the order request with the mock callback URL
        String requestBody = new ObjectMapper().writeValueAsString(demoOrderRequestDto);

        // 3. Send POST /orders to trigger async processing
        mockMvc.perform(post(orderEndPoint + methodEndPoint)
                        .headers(prepareAuthorisationHeaders(customerPass, customerPass))
                        .contentType("text/plain;charset=ISO-8859-1")
                        .accept("text/plain, application/json, application/*+json, */*")
                        .content(requestBody))
                .andExpect(status().isAccepted());

        // 4. Use Awaitility to wait for the callback (max 5 seconds)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            // 5. Get the recorded callback request from the mock server
            okhttp3.mockwebserver.RecordedRequest recordedRequest = mockCallbackServer.takeRequest();

            // 6. Validate callback method and headers
            assertThat(recordedRequest.getMethod()).isEqualTo("POST");
            assertThat(recordedRequest.getHeader("Content-Type")).contains("text/plain");

            // 7. Parse callback payload and validate contents
            String callbackBody = recordedRequest.getBody().readUtf8();
            OrderCallBackDto callback = getDummyObjectMapper().readValue(callbackBody, OrderCallBackDto.class);

            assertThat(callback.getStatus()).isEqualTo(OrderStatus.PROCESSED);
            assertThat(callback.getOrderId()).isNotEmpty(); // Order ID is generated
        });
    }

    private OrderRequestDto getDemoOrderRequestDto(String callbackUrl) {
        List<ItemDto> items = new ArrayList<>();
        items.add(ItemDto.builder().id("test-item-id-AAA").description("test-item-descAAA").price(new BigDecimal("123.45")).quantity(BigDecimal.TEN).build());
        items.add(ItemDto.builder().id("test-item-id-BBB").description("test-item-desc-BBB").price(new BigDecimal("321.54")).quantity(BigDecimal.ONE).build());
        return OrderRequestDto.builder()
                .requestId("test-requestId")
                .items(items)
                .callbackUrl(callbackUrl)
                .build();
    }

    private OrderCallBackDto getOrderCallBackDto(String customerId, String orderId) {
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

    private ObjectMapper getDummyObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Allow empty strings to be treated as null for object types
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        return objectMapper;
    }

}