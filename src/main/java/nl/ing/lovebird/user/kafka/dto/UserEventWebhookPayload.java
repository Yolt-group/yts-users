package nl.ing.lovebird.user.kafka.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class UserEventWebhookPayload {
    @NonNull
    @ApiModelProperty(required = true, value = "The webhook identifier.", example = "dacba0a6-2305-4359-b942-fea028602a7b")
    UUID webhookId;

    @NonNull
    @ApiModelProperty(required = true, value = "The internal user identifier.", example = "dacba0a6-2305-4359-b942-fea028602a7b")
    UUID userId;

    @NonNull
    @ApiModelProperty(required = true, value = "Human readable message for the action", example = "User deleted")
    String message;

    @NonNull
    @ApiModelProperty(required = true, value = "The event that happened to the user", example = "DELETED")
    Event event;

    public enum Event {
        ONE_OFF_USER_PROMOTED_TO_RECURRING, DELETED, BLOCKED, UNBLOCKED
    }
}
