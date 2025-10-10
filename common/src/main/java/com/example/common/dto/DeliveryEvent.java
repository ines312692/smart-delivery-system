import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DeliveryEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;

    private Long deliveryId;
    private String deliveryNumber;
    private Long orderId;
    private String orderNumber;
    private Long paymentId;
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String status;
    private Long agentId;
    private String agentName;
    private String agentPhone;
    private LocationDTO currentLocation;
    private LocationDTO destinationLocation;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private String trackingUrl;
}