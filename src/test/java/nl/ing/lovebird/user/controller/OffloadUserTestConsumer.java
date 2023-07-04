package nl.ing.lovebird.user.controller;

import lombok.Getter;
import nl.ing.lovebird.user.datascience.OffloadUserMessage;
import nl.ing.lovebird.user.datascience.UserData;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OffloadUserTestConsumer {

    @Getter
    private final List<String> keys = new ArrayList<>();

    @KafkaListener(topics = "${yolt.kafka.topics.offload-users.topic-name}", concurrency = "${yolt.kafka.topics.offload-users.listener-concurrency}")
    public void consume(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key, @Payload ConsumerRecord<OffloadUserMessage<UserData>, byte[]> value) {
        keys.add(key);
    }

    public void clear() {
        keys.clear();
    }

}
