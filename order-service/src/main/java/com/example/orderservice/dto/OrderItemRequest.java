package com.delivery.order.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

    @NotBlank(message = "Product ID is required")
    private String productId;

    @NotBlank(message = "Product name is required")
    private String productName;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal unitPrice;
}
