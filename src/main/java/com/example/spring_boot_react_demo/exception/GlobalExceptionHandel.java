package com.example.spring_boot_react_demo.exception;
import com.example.spring_boot_react_demo.model.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandel {

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException ae) {
        ErrorCode errorCode = ae.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(ae.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .body(apiResponse);
    }
}