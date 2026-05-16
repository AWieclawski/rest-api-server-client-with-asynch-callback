package edu.orders.client.data.mapper;

import edu.orders.client.data.model.ApiError;
import edu.orders.client.dto.ApiErrorDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiErrorMapper {

    public static ApiError toEntity(ApiErrorDto dto, String errorId) {
        return dto != null ? ApiError.builder()
                .status(dto.getStatus())
                .errors(dto.getErrors())
                .message(dto.getMessage())
                .errorId(errorId)
                .build()
                : null;
    }

    public static ApiErrorDto toDto(ApiError entity) {
        return entity != null ? ApiErrorDto.builder()
                .status(entity.getStatus())
                .errors(entity.getErrors())
                .message(entity.getMessage())
                .build()
                : null;
    }
}
