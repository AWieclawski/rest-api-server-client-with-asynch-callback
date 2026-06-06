package edu.orders.server.crypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.orders.server.data.model.Customer;
import edu.orders.server.data.repository.CustomersRepository;
import edu.orders.server.dto.ApiErrorDto;
import edu.orders.server.dto.OrderCallBackDto;
import edu.orders.server.dto.OrderHeader;
import edu.orders.server.dto.OrderRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CryptoServerConverter {

    final CustomersRepository customersRepository;

    final CryptoService cryptoService;

    public OrderRequestDto convertRequestBody(HttpServletRequest requestEntity) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        Collections.list(requestEntity.getHeaderNames()).forEach(it -> headers.set(it, requestEntity.getHeader(it)));
        log.debug("Client Request Headers {}", headers);
        requestVerification(new CryptoHeadersDto(headers));
        if ("POST".equalsIgnoreCase(requestEntity.getMethod())) {
            String rawBody = requestEntity.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            return deserializeOrderRequest(rawBody, headers);
        }
        throw new RuntimeException("Request method not supported: " + requestEntity.getMethod());
    }

    public HttpEntity<String> getCallBackServerResponse(OrderCallBackDto orderCallBackDto, String orderRequestId) {
        Customer customer = customersRepository.findByCustomerId(orderCallBackDto.getCustomerId());
        HttpHeaders preparedAuthorisationHeaders = cryptoService.prepareAuthorisationHeaders(customer.getPassword(), orderCallBackDto.getCustomerId());
        preparedAuthorisationHeaders.set(OrderHeader.REQUEST_ID.getValue(), orderRequestId);
        log.debug("Server Response Headers {}", preparedAuthorisationHeaders);
        String encodedBody = getOrderEncodedBody(orderCallBackDto, preparedAuthorisationHeaders);
        return new HttpEntity<>(encodedBody, preparedAuthorisationHeaders);
    }

    public HttpEntity<String> getErrorServerResponse(OrderRequestDto orderRequestDto, ApiErrorDto requestError) throws JsonProcessingException {
        String valueAsString = new ObjectMapper().writeValueAsString(requestError);
        HttpHeaders headers = new HttpHeaders();
        headers.set(OrderHeader.ORDER_ID.getValue(), orderRequestDto.getRequestId());
        headers.set(OrderHeader.ERROR_RESPONSE.getValue(), "TRUE");
        return new HttpEntity<>(valueAsString, headers);
    }

    private void requestVerification(CryptoHeadersDto cryptoHeadersDto) {
        Customer customer = customersRepository.findByCustomerId(cryptoHeadersDto.customer);
        String decryptedAuthorisation = cryptoService.decryptAuthorisation(cryptoHeadersDto);
        if (!decryptedAuthorisation.equals(customer.getPassword())) {
            throw new RuntimeException("Authentication failed for customer id: " + customer.getCustomerId());
        }
    }

    private OrderRequestDto deserializeOrderRequest(String rawBody, HttpHeaders headers) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(rawBody, OrderRequestDto.class);
        } catch (JsonProcessingException e) {
            return (OrderRequestDto) cryptoService.decryptDto(rawBody, OrderRequestDto.class, headers);
        }
    }

    private <T> String getOrderEncodedBody(T dto, HttpHeaders httpHeaders) {
        try {
            return cryptoService.encryptDto(dto, httpHeaders);
        } catch (Exception ex) {
            log.error("OrderEncodedBody building failed! {}: {} ", ex.getClass(), ex.getMessage(), ex.getCause());
        }
        return null;
    }
}
