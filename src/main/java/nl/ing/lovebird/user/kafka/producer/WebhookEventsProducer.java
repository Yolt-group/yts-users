package nl.ing.lovebird.user.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import nl.ing.lovebird.user.kafka.dto.UserEventWebhookPayload;
import nl.ing.lovebird.user.kafka.dto.WebhookEventEnvelope;
import nl.ing.lovebird.user.kafka.dto.WebhookEventType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Service
public class WebhookEventsProducer {

    private final Clock clock;
    private final KafkaTemplate<String, WebhookEventEnvelope> kafkaTemplate;
    private final String topic;

    public WebhookEventsProducer(final Clock clock,
                                           final KafkaTemplate<String, WebhookEventEnvelope> kafkaTemplate,
                                           @Value("${yolt.kafka.topics.webhooks.topic-name}") final String topic) {
        this.clock = clock;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendWebhook(final UserEventWebhookPayload payload,
                            final ClientUserToken clientUserToken) {
        UUID clientId = clientUserToken.getClientIdClaim();
        UUID userId = clientUserToken.getUserIdClaim();
        final Message<WebhookEventEnvelope> message = MessageBuilder
                .withPayload(buildWebHookEnvelope(clientId, userId, payload))
                .setHeader(KafkaHeaders.MESSAGE_KEY, userId.toString())
                .setHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, clientUserToken.getSerialized())
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();

        kafkaTemplate.send(message);

        log.info("Webhook event sent: type={}, clientId={}, userId={}", WebhookEventType.USER, clientId, userId);
    }

    private WebhookEventEnvelope buildWebHookEnvelope(final UUID clientId,
                                                      final UUID userId,
                                                      final UserEventWebhookPayload payload) {
        return WebhookEventEnvelope.builder()
                .clientId(clientId)
                .userId(userId)
                .webhookEventType(WebhookEventType.USER)
                .submittedAt(ZonedDateTime.now(clock))
                .payload(payload)
                .build();
    }
}
