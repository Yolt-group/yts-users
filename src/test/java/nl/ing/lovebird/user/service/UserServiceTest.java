package nl.ing.lovebird.user.service;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.user.controller.UserDTOMapper;
import nl.ing.lovebird.user.controller.dto.UserDTO;
import nl.ing.lovebird.user.datascience.UserSyncService;
import nl.ing.lovebird.user.kafka.producer.WebhookEventsProducer;
import nl.ing.lovebird.user.repository.postgres.UserRepository;
import nl.ing.lovebird.user.repository.postgres.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserEventsService userEventsService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private WebhookEventsProducer webhookEventsProducer;
    @Mock
    private UserDTOMapper userDTOMapper;
    @Mock
    private UserSyncService userSyncService;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @AfterEach
    void after() {
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    void shouldSendCorrectNewUserCreated() {
        //Given
        final var clientId = UUID.randomUUID();
        ClientToken clientToken = mock(ClientToken.class);
        when(clientToken.getClientIdClaim()).thenReturn(clientId);

        final var userDTO = mock(UserDTO.class);
        when(userDTOMapper.map(any(User.class)))
                .thenReturn(userDTO);

        //When
        final UserDTO newUser = userService.createNewUser(clientToken, true);

        //Then
        verify(userRepository).save(userCaptor.capture());
        final var user = userCaptor.getValue();
        final var expectedUser = new User(
                UUID.randomUUID(),
                clientId,
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                true,
                null
        );
        assertThat(user.getUserId()).isNotNull();
        assertThat(user.getClientUserId()).isNotNull();
        assertThat(user.getCreated()).isNotNull();
        assertThat(user)
                .usingRecursiveComparison()
                .ignoringFields("userId", "clientUserId", "created")
                .isEqualTo(expectedUser);
        verify(userEventsService).created(clientToken, user);
        verify(userDTOMapper).map(user);
        assertThat(newUser).isEqualTo(userDTO);
    }
}
