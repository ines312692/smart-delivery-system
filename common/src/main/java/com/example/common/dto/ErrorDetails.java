package com.delivery.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String message;
    private String field;
    private Map<String, String> validationErrors;
    private String stackTrace;
}
