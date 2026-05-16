package edu.orders.server.crypto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
class CryptoService {

    @Value("${crypto.auth.key}")
    private String cryptoKey;

    @Value("${crypto.auth.algorithm}")
    private String algorithm;

    @Value("${crypto.date.pattern}")
    private String datePattern;

    <T> String encryptDto(T dto, HttpHeaders headers) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        final String serialized = mapper.writeValueAsString(dto);
        return encryptInput(headers, serialized);
    }

    <T> Object decryptDto(String inputOrderCallBackDto, Class<T> contentClass, HttpHeaders headers) throws JsonProcessingException {
        String decrypted = decryptInput(headers, inputOrderCallBackDto);
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructType(contentClass);
        return mapper.readValue(decrypted, type);
    }

    String decryptInput(HttpHeaders headers, String input) {
        CryptoHeadersDto headersDto = getCryptoHeadersDto(headers);
        try {
            assert headersDto != null;
            headersDto.validateToDecrypt();
            return decryptInput(headersDto.date, headersDto.ivKey, input);
        } catch (Throwable ex) {
            log.error("Encrypt authorisation error {}: {} ", ex.getClass(), ex.getMessage(), ex.getCause());
        }
        return null;
    }

    String encryptInput(HttpHeaders headers, String input) {
        CryptoHeadersDto headersDto = getCryptoHeadersDto(headers);
        try {
            assert headersDto != null;
            headersDto.validateToEncrypt();
            return encryptPass(headersDto.date, headersDto.ivKey, input);
        } catch (Throwable ex) {
            log.error("Encrypt authorisation error {}: {} ", ex.getClass(), ex.getMessage(), ex.getCause());
        }
        return null;
    }

    String decryptAuthorisation(CryptoHeadersDto headersDto) {
        try {
            return decryptInput(headersDto.date, headersDto.ivKey, headersDto.pass);
        } catch (Exception ex) {
            log.error("Decrypt authorisation error {}: {} ", ex.getClass(), ex.getMessage(), ex.getCause());
        }
        return null;
    }

    HttpHeaders prepareAuthorisationHeaders(String password, String customerId) {
        HttpHeaders headers = new HttpHeaders();
        String generateIvString = CryptoUtils.generateIvString();
        String formattedDate = getFormattedDate();
        headers.set(CryptoHeader.DATE_KEY.getValue(), formattedDate);
        headers.set(CryptoHeader.IV_KEY.getValue(), generateIvString);
        headers.set(CryptoHeader.CUSTOMER_ID.getValue(), customerId);
        headers.set(CryptoHeader.PASS_KEY.getValue(), encryptInput(headers, password));
        return headers;
    }

    private String getFormattedDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(datePattern));
    }

    private CryptoHeadersDto getCryptoHeadersDto(HttpHeaders headers) {
        try {
            return new CryptoHeadersDto(headers);
        } catch (Exception ex) {
            log.error("CryptoHeadersDto building failed! {}: {} ", ex.getClass(), ex.getMessage(), ex.getCause());
        }
        return null;
    }

    private String encryptPass(String date, String iv, String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        SecretKey key = CryptoUtils.getKeyFromPassword(cryptoKey, date);
        GCMParameterSpec gcmParameterSpec = CryptoUtils.getIvFromBase64(iv);
        return CryptoUtils.encryptAESPasswordBased(password, key, gcmParameterSpec, algorithm);
    }

    private String decryptInput(String date, String iv, String inputText)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        SecretKey key = CryptoUtils.getKeyFromPassword(cryptoKey, date);
        GCMParameterSpec gcmParameterSpec = CryptoUtils.getIvFromBase64(iv);
        return CryptoUtils.decryptAESPasswordBased(inputText, key, gcmParameterSpec, algorithm);
    }

}
