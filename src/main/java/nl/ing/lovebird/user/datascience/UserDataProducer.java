package nl.ing.lovebird.user.datascience;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Slf4j
@Service
@Validated
public class UserDataProducer {

    private static final int MESSAGE_VERSION = 2;

    private final KafkaTemplate<String, OffloadUserMessage<UserData>> kafkaTemplate;
    private final String topic;

    public UserDataProducer(KafkaTemplate<String, OffloadUserMessage<UserData>> kafkaTemplate,
                            @Value("${yolt.kafka.topics.offload-users.topic-name}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    void sendMessage(ClientToken clientToken, UserData userData) {
        OffloadUserMessage<UserData> payload = new OffloadUserMessage<>(userData.getClientId(), userData.getUserId(), MESSAGE_VERSION, userData);
        sendMessage(clientToken, payload);
    }

    private void sendMessage(ClientToken clientToken, @Valid OffloadUserMessage<UserData> payload) {
        Message<OffloadUserMessage<UserData>> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.MESSAGE_KEY, payload.getEntity_id())
                .setHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, clientToken != null ? clientToken.getSerialized() : null)
                .build();
        kafkaTemplate.send(message);

        log.info("Send user with entity_id {} to data science.", payload.getEntity_id());
    }
}
