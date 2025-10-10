import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String source;
    private String version;

    public void prepareEvent(String eventType, String source) {
        this.eventType = eventType;
        this.source = source;
        this.timestamp = LocalDateTime.now();
        this.version = "1.0";
    }
}
