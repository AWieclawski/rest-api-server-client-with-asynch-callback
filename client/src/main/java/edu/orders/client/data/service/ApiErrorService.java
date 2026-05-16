package edu.orders.client.data.service;

import edu.orders.client.data.mapper.ApiErrorMapper;
import edu.orders.client.data.model.ApiError;
import edu.orders.client.data.repository.ApiErrorRepository;
import edu.orders.client.dto.ApiErrorDto;
import edu.orders.client.service.BaseDateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@Service(ApiErrorService.BEAN_NAME)
@RequiredArgsConstructor
@Slf4j
@DependsOn(ApiErrorRepository.BEAN_NAME)
public class ApiErrorService {

    public static final String BEAN_NAME = "edu.orders.client.data.service.ApiErrorService";

    private final ApiErrorRepository apiErrorRepository;
    private final BaseDateService baseDateService;


    public ApiError saveError(ApiErrorDto apiErrorDto) {
        ApiError result = null;
        try {
            result = apiErrorRepository.insertError(ApiErrorMapper.toEntity(apiErrorDto, baseDateService.getBaseTimestampId()));
            log.info("ApiError [{}] saved successfully", result);
        } catch (Exception e) {
            log.error("ApiError persistence process error! {}", e.getMessage(), e);
        }
        return result;
    }

}
