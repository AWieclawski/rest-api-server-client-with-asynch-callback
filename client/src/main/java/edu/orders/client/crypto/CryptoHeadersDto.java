package edu.orders.client.crypto;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import java.util.List;

@Slf4j
@ToString
class CryptoHeadersDto {
    String date;
    String ivKey;
    String pass;
    String customer;

    CryptoHeadersDto(HttpHeaders headers) {
        date = getValueFromHeader(headers, CryptoHeader.DATE_KEY.getValue());
        ivKey = getValueFromHeader(headers, CryptoHeader.IV_KEY.getValue());
        pass = getValueFromHeader(headers, CryptoHeader.PASS_KEY.getValue());
        customer = getValueFromHeader(headers, CryptoHeader.CUSTOMER_ID.getValue());
    }

    public void validateToDecrypt() {
        if (date == null || ivKey == null) {
            log.error("Headers analise failed. date [{}] iv: [{}]",
                    date, ivKey);
            throw new RuntimeException("Headers analise error!");
        }
    }

    public void validateToEncrypt() {
        if (date == null || ivKey == null || customer == null) {
            log.error("Headers analise failed. date [{}] iv: [{}] customer: [{}]",
                    date, ivKey, customer);
            throw new RuntimeException("Headers analise error!");
        }
    }

    private String getValueFromHeader(HttpHeaders headers, String headerName) {
        List<String> headerList = headers.get(headerName);
        return headerList != null && !headerList.isEmpty() ? headerList.get(0) : null;
    }

}

