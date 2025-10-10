import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationEvent extends BaseEvent {
    private static final long serialVersionUID = 1L;

    private String notificationType; // EMAIL, SMS, PUSH
    private String recipient;
    private String subject;
    private String message;
    private String templateName;
    private Map<String, Object> templateData;
    private String priority; // HIGH, MEDIUM, LOW
    private String relatedEntityType; // ORDER, PAYMENT, DELIVERY
    private String relatedEntityId;
}
