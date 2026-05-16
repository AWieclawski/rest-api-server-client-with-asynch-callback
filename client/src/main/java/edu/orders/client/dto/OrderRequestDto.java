package edu.orders.client.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
    private String requestId;
    private List<ItemDto> items;
    @Setter
    private String callbackUrl;
    @Setter
    private String customerId;
}
