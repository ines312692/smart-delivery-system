import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;

    private Long orderId;
    private String orderNumber;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemDTO> items;
}
