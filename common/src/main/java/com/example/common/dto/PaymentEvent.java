import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;

    private Long paymentId;
    private String paymentNumber;
    private Long orderId;
    private String orderNumber;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionId;
    private String failureReason;
    private Integer retryCount;
}
