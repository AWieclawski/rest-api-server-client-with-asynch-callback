package edu.orders.server.dto;

import lombok.Getter;

public enum OrderHeader {
    ORDER_ID("X-Order-Id"),
    REQUEST_ID("X-Request-Id"),
    ERROR_RESPONSE("X-Error-Response");

    @Getter
    private final String value;

    OrderHeader(String value) {
        this.value = value;
    }
}
