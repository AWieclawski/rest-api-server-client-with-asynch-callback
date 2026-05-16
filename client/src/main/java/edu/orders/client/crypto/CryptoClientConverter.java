package edu.orders.client.crypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.orders.client.dto.ApiErrorDto;
import edu.orders.client.dto.OrderCallBackDto;
import edu.orders.client.dto.OrderRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@Component(CryptoClientConverter.BEAN_NAME)
@RequiredArgsConstructor
@DependsOn(CryptoService.BEAN_NAME)
public class CryptoClientConverter {

    public final static String BEAN_NAME = "edu.orders.client.crypto.CryptoClientConverter";

    final CryptoService cryptoService;

    @Value("${order-client.pass}")
    private String customerPass;

    @Value("${order-client.id}")
    private String customerId;

    @Value("${order-client.callback.url}")
    private String clientCallbackUrl;

    public HttpEntity<String> getOrderCustomerRequest(OrderRequestDto orderRequestDto) {
        orderRequestDto.setCustomerId(customerId);
        orderRequestDto.setCallbackUrl(clientCallbackUrl);
        HttpHeaders preparedAuthorisationHeaders = cryptoService.prepareAuthorisationHeaders(customerPass, customerId);
        log.debug("Client Request Headers {}", preparedAuthorisationHeaders.toString());
        String encodedBody = getOrderEncodedBody(orderRequestDto, preparedAuthorisationHeaders);
        return new HttpEntity<>(encodedBody, preparedAuthorisationHeaders);
    }

    public OrderCallBackDto convertToOrderCallBackDto(HttpServletRequest requestEntity) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        Collections.list(requestEntity.getHeaderNames()).forEach(it -> headers.set(it, requestEntity.getHeader(it)));
        log.debug("Server Response Headers {}", headers);
        responseVerification(new CryptoHeadersDto(headers));
        if ("POST".equalsIgnoreCase(requestEntity.getMethod())) {
            String rawBody = requestEntity.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            return deserializeOrderCallBackDto(rawBody, headers);
        }
        throw new RuntimeException("Request method not supported: " + requestEntity.getMethod());
    }

    public ApiErrorDto convertToApiErrorDto(HttpServletRequest requestEntity) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        Collections.list(requestEntity.getHeaderNames()).forEach(it -> headers.set(it, requestEntity.getHeader(it)));
        log.debug(headers.toString());
        if ("POST".equalsIgnoreCase(requestEntity.getMethod())) {
            String rawBody = requestEntity.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            return deserializeApiErrorDto(rawBody);
        }
        throw new RuntimeException("Request method not supported: " + requestEntity.getMethod());
    }

    private ApiErrorDto deserializeApiErrorDto(String rawBody) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(rawBody, ApiErrorDto.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private void responseVerification(CryptoHeadersDto cryptoHeadersDto) {
        String decryptedAuthorisation = cryptoService.decryptAuthorisation(cryptoHeadersDto);
        if (!decryptedAuthorisation.equals(customerPass)) {
            throw new RuntimeException("Response verification failed!");
        }
    }

    private OrderCallBackDto deserializeOrderCallBackDto(String rawBody, HttpHeaders headers) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(rawBody, OrderCallBackDto.class);
        } catch (JsonProcessingException e) {
            return (OrderCallBackDto) cryptoService.decryptDto(rawBody, OrderCallBackDto.class, headers);
        }
    }

    private <T> String getOrderEncodedBody(T dto, HttpHeaders httpHeaders) {
        try {
            return cryptoService.encryptDto(dto, httpHeaders);
        } catch (Exception ex) {
            log.error("OrderEncodedBody building failed! class: {} message: {} ", ex.getClass(), ex.getMessage(), ex.getCause());
        }
        return null;
    }
}
