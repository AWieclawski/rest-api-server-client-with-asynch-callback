package edu.orders.server.controller;

import edu.orders.server.dto.ApiErrorDto;
import edu.orders.server.dto.OrderHeader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorHelper {

    public static ApiErrorDto requestErrorBuild(Throwable ex, HttpServletRequest requestEntity) {
        Enumeration<String> requestIds = requestEntity.getHeaders(OrderHeader.REQUEST_ID.getValue());
        String requestId = requestIds != null ? requestIds.nextElement() : null;
        return getApiErrorDto(ex, requestId);
    }

    public static ApiErrorDto getApiErrorDto(Throwable ex, String requestId) {
        List<String> errors = getErrorMessages(ex);
        return ApiErrorDto.builder()
                .errors(errors)
                .status(HttpStatus.BAD_REQUEST)
                .message("Failed request id: " + requestId)
                .build();
    }

    private static List<String> getErrorMessages(Throwable ex) {
        List<String> errors = new ArrayList<>();
        String errorMessage = ex.getClass() + " message" + ex.getMessage();
        errors.add(errorMessage);
        return errors;
    }
}
