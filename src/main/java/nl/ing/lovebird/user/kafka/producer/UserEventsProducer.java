package nl.ing.lovebird.user.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import nl.ing.lovebird.user.kafka.dto.UserUpdateDTO;
import nl.ing.lovebird.user.kafka.dto.UserUpdateType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class UserEventsProducer {

    private static final long SEND_MESSAGE_TIMEOUT_SECONDS = 5L;

    private final KafkaTemplate<String, UserUpdateDTO> kafkaTemplate;
    private final String topicUserUpdate;

    public UserEventsProducer(final KafkaTemplate<String, UserUpdateDTO> kafkaTemplate,
                              @Value("${yolt.kafka.topics.users.topic-name}") final String topicUserUpdate) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicUserUpdate = topicUserUpdate;
    }

    public void sendMessage(ClientToken clientToken, UserUpdateDTO userUpdateDTO) {
        final UUID userId = userUpdateDTO.getHeaders().getUserId();
        Message<UserUpdateDTO> message = MessageBuilder
                .withPayload(userUpdateDTO)
                .setHeader(KafkaHeaders.TOPIC, topicUserUpdate)
                .setHeader(KafkaHeaders.MESSAGE_KEY, userId.toString())
                // NOTE: This can be either a client-user-token or if the user was created a client token.
                // It is not possible to create a client-user-token before a user is created.
                .setHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, clientToken.getSerialized())
                .setHeader("user-update-type", UserUpdateType.OTHER.name())
                .build();

        try {
            kafkaTemplate.send(message).get(SEND_MESSAGE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            log.info("Published {} message (update type {})", userUpdateDTO.getHeaders().getMessageType(), UserUpdateType.OTHER.name()); //NOSHERIFF
        } catch (InterruptedException e) {
            String errorMessage = String.format("Interrupted while publishing message for user %s", userId);
            log.error(errorMessage, e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            String errorMessage = String.format("Publishing message failed for user %s", userId);
            log.error(errorMessage, e);
            throw new PublishingEventFailedException(e);
        } catch (TimeoutException e) {
            String errorMessage = String.format("Timed out while publishing message for user %s, timeout is %d seconds",
                    userId, SEND_MESSAGE_TIMEOUT_SECONDS);
            log.error(errorMessage, e);
            throw new PublishingEventFailedException(e);
        }
    }

}
