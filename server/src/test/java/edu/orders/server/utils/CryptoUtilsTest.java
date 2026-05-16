package edu.orders.server.utils;

/**
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.orders.server.crypto.CryptoUtils;
import edu.orders.server.data.model.OrderStatus;
import edu.orders.server.dto.ItemDto;
import edu.orders.server.dto.OrderCallBackDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
 */

class CryptoUtilsTest {

    private final String KEY = "qwerty";
    private final String ALGORITHM = "AES/GCM/NoPadding";

    /**
    @Test
    void test_simple_String_EncryptBase64() {
        final String value = "test";
        String encrypt = CryptoUtils.encryptBase64(KEY, value);
        assertNotEquals(value, encrypt);
        String decrypt = CryptoUtils.decryptBase64(KEY, encrypt);
        assertEquals(value, decrypt);
    }

    @Test
    void test_Serialized_Objects_EncryptBase64() throws JsonProcessingException {
        ItemDto itemDto01 = ItemDto.builder()
                .code("2026211234567890")
                .price(BigDecimal.ONE)
                .quantity(BigDecimal.TEN)
                .description("Description01")
                .build();
        ItemDto itemDto02 = ItemDto.builder()
                .code("2026210987654321")
                .price(BigDecimal.TEN)
                .quantity(BigDecimal.ONE)
                .description("Description02")
                .build();
        OrderCallBackDto orderCallBackDto = OrderCallBackDto.builder()
                .orderId("2026042136987456321")
                .requestId("requestId")
                .customerId("customerId")
                .items(Arrays.asList(itemDto01, itemDto02))
                .status(OrderStatus.PROCESSED)
                .build();
        ObjectMapper mapper = new ObjectMapper();
        final String value = mapper.writeValueAsString(orderCallBackDto);
        String encrypt = CryptoUtils.encryptBase64(KEY, value);
        assertNotEquals(value, encrypt);
        String decrypt = CryptoUtils.decryptBase64(KEY, encrypt);
        assertEquals(value, decrypt);
        OrderCallBackDto orderCallBackDtoOutput = mapper.readValue(decrypt, OrderCallBackDto.class);
        assertEquals(orderCallBackDto.getOrderId(), orderCallBackDtoOutput.getOrderId());
        assertEquals(orderCallBackDto.getStatus(), orderCallBackDtoOutput.getStatus());
    }

    @Test
    void givenString_whenEncryptAES_thenSuccess()
            throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
            BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        String input = "test";
        SecretKey key = CryptoUtils.generateKey(128);
        GCMParameterSpec gcmParameterSpec = CryptoUtils.generateIv();
        String cipherText = CryptoUtils.encryptAES(ALGORITHM, input, key, gcmParameterSpec);
        assertNotEquals(input, cipherText);
        String plainText = CryptoUtils.decryptAES(ALGORITHM, cipherText, key, gcmParameterSpec);
        Assertions.assertEquals(input, plainText);
    }

    @Test
    void givenString_key_randomIv_salt_whenEncryptAES_thenSuccess()
            throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
            BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeySpecException {
        String plainText = "test.test.test";
        SecretKey key = CryptoUtils.getKeyFromPassword(KEY, "2026-06-13");
        GCMParameterSpec gcmParameterSpec = CryptoUtils.generateIv();
        String cipherText = CryptoUtils.encryptAESPasswordBased(plainText, key, gcmParameterSpec, ALGORITHM);
        assertNotEquals(plainText, cipherText);
        String decryptedCipherText = CryptoUtils.decryptAESPasswordBased(cipherText, key, gcmParameterSpec, ALGORITHM);
        Assertions.assertEquals(plainText, decryptedCipherText);
    }

    @Test
    void givenString_key_staticIv_salt_whenEncryptAES_thenSuccess()
            throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException,
            BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeySpecException {
        String plainText = "test.test.test";
        SecretKey key = CryptoUtils.getKeyFromPassword(KEY, "2026-06-13");
        GCMParameterSpec gcmParameterSpec = CryptoUtils.getIvFromBase64("02nUrRVha+c53gZhYg");
        String cipherText = CryptoUtils.encryptAESPasswordBased(plainText, key, gcmParameterSpec, ALGORITHM);
        assertNotEquals(plainText, cipherText);
        String decryptedCipherText = CryptoUtils.decryptAESPasswordBased(cipherText, key, gcmParameterSpec, ALGORITHM);
        Assertions.assertEquals(plainText, decryptedCipherText);
    }
    */
}