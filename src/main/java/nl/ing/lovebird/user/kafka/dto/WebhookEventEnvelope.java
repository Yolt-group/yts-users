package nl.ing.lovebird.user.kafka.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class WebhookEventEnvelope {

    @NonNull
    @ApiModelProperty(required = true, value = "The client identifier.", example = "dacba0a6-2305-4359-b942-fea028602a7b")
    UUID clientId;

    @NonNull
    @ApiModelProperty(required = true, value = "The internal user identifier.", example = "dacba0a6-2305-4359-b942-fea028602a7b")
    UUID userId;

    @NonNull
    @ApiModelProperty(required = true, value = "The webhook event type (e.a.)", example = "AIS")
    WebhookEventType webhookEventType;

    @NonNull
    @ToString.Exclude
    @ApiModelProperty(required = true, value = "The untyped (but JSON compatible) payload to be send to the webhook endpoint of the client.", example = "{...}")
    UserEventWebhookPayload payload;

    @NonNull
    @ApiModelProperty(required = true, value = "The moment this webhook event was send from the origin.", example = "2018-01-01T10:10:10.123[GMT]")
    ZonedDateTime submittedAt;

}
