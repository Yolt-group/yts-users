package nl.ing.lovebird.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.rest.deleteuser.UserDeleter;
import nl.ing.lovebird.user.datascience.UserSyncService;
import nl.ing.lovebird.user.kafka.dto.UserEventWebhookPayload;
import nl.ing.lovebird.user.kafka.producer.WebhookEventsProducer;
import nl.ing.lovebird.user.repository.postgres.UserRepository;
import nl.ing.lovebird.user.repository.postgres.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class UserDeleteService {

    private final UserRepository userRepository;
    private final UserEventsService userEventsService;
    private final WebhookEventsProducer webhookEventsProducer;
    private final UserSyncService userSyncService;
    private final Clock clock;

    @Autowired
    public void registerUserDeleter(final UserDeleter deleter) {
        deleter.registerDeleter(this::deleteUser);
    }

    private void deleteUser(final ClientUserToken clientUserToken) {

        UUID userId = clientUserToken.getUserIdClaim();
        final var userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            final var user = userOptional.get();
            sendUserDeletedWebhook(user, clientUserToken);
            sendUserDeletedEvent(clientUserToken);
            user.setDeleted(clock.instant());
            userRepository.save(user);
            userSyncService.syncUser(null, user);
        } else {
            log.warn("User {} not found, probably already deleted,", userId);
        }
    }

    private void sendUserDeletedEvent(final ClientUserToken clientUserToken) {
        final var user = new User();
        user.setUserId(clientUserToken.getUserIdClaim());
        user.setClientId(clientUserToken.getClientIdClaim());
        userEventsService.deleted(clientUserToken, user);
    }

    /**
     * Sends a user deleted webhook.
     * We can only send a webhook if the client_id is known.
     * This operation should be executed once at most, we only want to send a webhook our clients for a DELETE once.
     */
    private void sendUserDeletedWebhook(User user, ClientUserToken clientUserToken) {
        if (user.getDeleted() != null) {
            var webhookPayload = new UserEventWebhookPayload(UUID.randomUUID(),
                    user.getUserId(),
                    "User deleted.",
                    UserEventWebhookPayload.Event.DELETED);
            webhookEventsProducer.sendWebhook(webhookPayload, clientUserToken);
        }
    }
}
