package nl.ing.lovebird.user.datascience;

import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDataProducerTest {

    @Mock
    private KafkaTemplate<String, OffloadUserMessage<UserData>> kafkaTemplate;

    @Captor
    private ArgumentCaptor<Message<OffloadUserMessage<UserData>>> messageCaptor;

    private String topicName = "offload_users";

    private UserDataProducer userDataProducer;

    @BeforeEach
    public void setUp() {
        userDataProducer = new UserDataProducer(kafkaTemplate, topicName);
    }

    @Test
    public void sendsMessageCorrectlyToDatascience() {
        UUID userId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UserData userData = new UserData(clientId, userId, null, null, null, LocalDateTime.now().toInstant(ZoneOffset.UTC), false, null);

        ClientToken clientToken = mock(ClientToken.class);
        when(clientToken.getSerialized()).thenReturn(clientId.toString());
        userDataProducer.sendMessage(clientToken, userData);

        verify(kafkaTemplate).send(messageCaptor.capture());

        Message<OffloadUserMessage<UserData>> actualMessage = messageCaptor.getValue();
        assertThat(actualMessage.getHeaders().get(KafkaHeaders.MESSAGE_KEY)).isEqualTo(userData.getClientId() + ":" + userData.getUserId());
        assertThat(actualMessage.getHeaders().get(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME)).isEqualTo(clientId.toString());

        OffloadUserMessage<UserData> actualKYCData = actualMessage.getPayload();

        assertThat(actualKYCData.getEntity_id()).isEqualTo(clientId + ":" + userId);
        assertThat(actualKYCData.getSchema_version()).isEqualTo(2);
        assertThat(actualKYCData.getPayload().getUserId()).isEqualTo(userId);
    }
}
