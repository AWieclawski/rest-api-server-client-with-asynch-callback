package edu.orders.server.dto;

import lombok.*;
import org.springframework.http.HttpHeaders;

import java.util.List;

@Getter
@Builder
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
    private String requestId;
    private String customerId;
    private List<ItemDto> items;
    private String callbackUrl;
    private HttpHeaders headers;
}
