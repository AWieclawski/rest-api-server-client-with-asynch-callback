package edu.orders.client.data.model;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Builder
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class ApiError {
    private String errorId;
    private HttpStatus status;
    private String message;
    private List<String> errors;
}
