package nl.ing.lovebird.user.controller;

import lombok.Getter;
import lombok.Value;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.user.kafka.dto.UserUpdateDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserTestConsumer {

    @Getter
    private final List<UserReceived> userReceiveds = new ArrayList<>();

    @KafkaListener(topics = "${yolt.kafka.topics.users.topic-name}", concurrency = "${yolt.kafka.topics.users.listener-concurrency}")
    public void consume(
            @Header("user-update-type") String updateType,
            @Header("client-token") ClientToken clientToken,
            @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
            @Payload UserUpdateDTO value
    ) {
        userReceiveds.add(new UserReceived(updateType, clientToken, key, value));
    }

    public void clear() {
        userReceiveds.clear();
    }

    @Value
    public static class UserReceived {
        String updateType;
        ClientToken clientToken;
        String key;
        UserUpdateDTO value;
    }
}
