package nl.ing.lovebird.user.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserUpdateDTO {

    private UserMessage payload;
    private Headers headers;

    @Builder(builderMethodName = "userUpdateBuilder")
    public UserUpdateDTO(Headers headers, UserMessage payload) {
        this.payload = payload;
        this.headers = headers;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Headers {

        @NotNull
        private UUID userId;

        @NotNull
        private UUID requestTraceId;

        @NotNull
        private String messageType;
    }
}