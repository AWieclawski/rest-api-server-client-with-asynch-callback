package edu.orders.client.dto;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Builder
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class ApiErrorDto {
    private HttpStatus status;
    private String message;
    private List<String> errors;
}
