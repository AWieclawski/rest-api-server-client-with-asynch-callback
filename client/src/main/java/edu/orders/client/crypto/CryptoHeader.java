package edu.orders.client.crypto;

import lombok.Getter;

enum CryptoHeader {
    PASS_KEY("C-Base-Auth"),
    DATE_KEY("C-Base-Date"),
    IV_KEY("C-Base-IV"),
    CUSTOMER_ID("C-Customer-Id");

    @Getter
    final private String value;

    CryptoHeader(String value) {
        this.value = value;
    }
}
