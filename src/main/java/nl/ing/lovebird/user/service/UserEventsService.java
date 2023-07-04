package nl.ing.lovebird.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.user.kafka.dto.UserMessage;
import nl.ing.lovebird.user.kafka.dto.UserMessageTypes;
import nl.ing.lovebird.user.kafka.dto.UserUpdateDTO;
import nl.ing.lovebird.user.kafka.producer.UserEventsProducer;
import nl.ing.lovebird.user.repository.domain.UserStatusType;
import nl.ing.lovebird.user.repository.postgres.model.User;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventsService {

    private final UserEventsProducer userEventsProducer;

    public void created(ClientToken clientToken, final User user) {
        UserUpdateDTO userUpdateDTO = map(user, UserMessageTypes.USER_CREATED);
        userEventsProducer.sendMessage(clientToken, userUpdateDTO);
    }

    public void updated(ClientUserToken clientUserToken, final User user) {
        UserUpdateDTO userUpdateDTO = map(user, UserMessageTypes.USER_UPDATED);
        userEventsProducer.sendMessage(clientUserToken, userUpdateDTO);
    }

    public void deleted(ClientUserToken clientUserToken, final User user) {
        UserUpdateDTO userUpdateDTO = map(user, UserMessageTypes.USER_DELETED);
        userEventsProducer.sendMessage(clientUserToken, userUpdateDTO);
    }

    private UserUpdateDTO map(User user, UserMessageTypes userMessageTypes) {
        final UserMessage kafkaUserMessage;
        if (userMessageTypes != UserMessageTypes.USER_DELETED) {
            kafkaUserMessage = getKafkaUserDTO(user);
        } else {
            kafkaUserMessage = UserMessage.builder()
                    .id(user.getUserId())
                    .clientId(user.getClientId())
                    .build();
        }

        return UserUpdateDTO.userUpdateBuilder()
                .headers(UserUpdateDTO.Headers.builder()
                        .messageType(userMessageTypes.name())
                        // trace id propagation is handled by sleuth.
                        // This value should be ignored by consumers
                        // This value should be consistent because it is part of a kafka key used to guarantee ordering
                        .requestTraceId(new UUID(0, 0))
                        .userId(user.getUserId())
                        .build())
                .payload(kafkaUserMessage)
                .build();
    }

    private UserMessage getKafkaUserDTO(final User user) {
        return UserMessage.builder()
                .id(user.getUserId())
                .clientId(user.getClientId())
                .created(ZonedDateTime.ofInstant(user.getCreated(), ZoneOffset.UTC))
                .countryCode("NL")
                .status(user.getBlocked() == null ? UserStatusType.ACTIVE : UserStatusType.BLOCKED)
                .preferredCurrency("EUR")
                .locale(Locale.UK)
                .oneOffAis(user.isOneOffAis())
                .build();
    }
}
