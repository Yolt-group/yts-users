package nl.ing.lovebird.user.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerSentEvent {

    private UUID userId;
    private EventName eventName;
    private EventData eventData;

    @AllArgsConstructor
    @Getter
    public enum EventName {
        @JsonProperty("kycStatus")
        KYC_STATUS
    }

    @AllArgsConstructor
    @Getter
    public enum EventData {
        @JsonProperty("updated")
        UPDATED
    }
}

