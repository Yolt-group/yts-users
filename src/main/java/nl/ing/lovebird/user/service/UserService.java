package nl.ing.lovebird.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.logging.MDCContextCreator;
import nl.ing.lovebird.user.clientusers.dto.UserIdsDTO;
import nl.ing.lovebird.user.controller.UserDTOMapper;
import nl.ing.lovebird.user.controller.dto.BlockUserRequestDTO;
import nl.ing.lovebird.user.controller.dto.UserCountDTO;
import nl.ing.lovebird.user.controller.dto.UserDTO;
import nl.ing.lovebird.user.datascience.UserSyncService;
import nl.ing.lovebird.user.exception.OneOffUserException;
import nl.ing.lovebird.user.exception.UserNotFoundException;
import nl.ing.lovebird.user.kafka.dto.UserEventWebhookPayload;
import nl.ing.lovebird.user.kafka.producer.WebhookEventsProducer;
import nl.ing.lovebird.user.repository.postgres.UserRepository;
import nl.ing.lovebird.user.repository.postgres.model.User;
import nl.ing.lovebird.user.service.domain.CreateUserCommand;
import nl.ing.lovebird.user.service.domain.UpdateUserCommand;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static nl.ing.lovebird.user.kafka.dto.UserEventWebhookPayload.Event.BLOCKED;
import static nl.ing.lovebird.user.kafka.dto.UserEventWebhookPayload.Event.UNBLOCKED;
import static nl.ing.lovebird.user.logging.Tracing.USER_ID_TRACE;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserService {

    public static final String ONE_OFF_AIS_USER_PROMOTED_MESSAGE = "One-off AIS user promoted to recurring AIS user.";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ", Locale.US).withZone(ZoneOffset.UTC);
    private final UserRepository userRepository;
    private final UserEventsService userEventsService;
    private final WebhookEventsProducer webhookEventsProducer;
    private final UserDTOMapper userDTOMapper;
    private final UserSyncService userSyncService;
    private final MaintenanceClient maintenanceClient;
    private final Clock clock;

    @Deprecated
    public UserDTO createNewDeprecatedUser(ClientToken clientToken, final @Valid CreateUserCommand createUserCommand) {
        final User user = new User(
                UUID.randomUUID(),
                createUserCommand.getClientId(),
                null,
                null,
                null,
                null,
                new Date().toInstant(),
                createUserCommand.isOneOffAis(),
                null
        );
        return createNewUserInternal(user, clientToken);
    }

    public UserDTO createNewUser(ClientToken clientToken, boolean oneOffAIS) {
        final User user = new User(
                UUID.randomUUID(),
                clientToken.getClientIdClaim(),
                UUID.randomUUID(),
                null,
                null,
                null,
                new Date().toInstant(),
                oneOffAIS,
                null
        );

        return createNewUserInternal(user, clientToken);
    }

    private UserDTO createNewUserInternal(User user, ClientToken clientToken) {
        try {
            USER_ID_TRACE.updateValue(user.getUserId().toString());
            MDC.put(MDCContextCreator.USER_ID_HEADER_NAME, user.getUserId().toString());
            log.info("Creating user");

            userRepository.save(user);

            userEventsService.created(clientToken, user);
            userSyncService.syncUser(clientToken, user);

            log.debug("Created user {}", user);
            return userDTOMapper.map(user);
        } finally {
            USER_ID_TRACE.updateValue(null);
            MDC.remove(MDCContextCreator.USER_ID_HEADER_NAME);
        }
    }

    public void updateUser(final ClientUserToken clientUserToken, final @Valid UpdateUserCommand updateUserCommand) {

        final var userId = updateUserCommand.getUserId();

        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with user id " + userId + " does not exist"));

        var oldStateIsOneOffAIS = user.isOneOffAis();
        final var isNotAllowedToChange = updateUserCommand.isOneOffAis() && !oldStateIsOneOffAIS;

        if (isNotAllowedToChange) {
            throw new OneOffUserException("A regular user can not be promoted to an one-off one");
        }

        try {
            USER_ID_TRACE.updateValue(user.getUserId().toString());
            MDC.put(MDCContextCreator.USER_ID_HEADER_NAME, user.getUserId().toString());
            log.info("Update user");

            user.setOneOffAis(updateUserCommand.isOneOffAis());
            userRepository.save(user);

            userEventsService.updated(clientUserToken, user);

            userSyncService.syncUser(clientUserToken, user);

            if (!user.isOneOffAis() && oldStateIsOneOffAIS) {
                sendOneOffAISUserPromotedWebhook(user, clientUserToken);
            }

            log.debug("Updated user {}", user);
        } finally {
            USER_ID_TRACE.updateValue(null);
            MDC.remove(MDCContextCreator.USER_ID_HEADER_NAME);
        }
    }

    public UserCountDTO countByClientId(UUID clientId) {

        final var usersByClient = userRepository.findAllByClientIdAndDeletedIsNull(clientId);
        final var count = usersByClient.size();
        final var oneOffAisUsersCount = usersByClient.stream()
                .filter(User::isOneOffAis)
                .count();
        return new UserCountDTO(count, oneOffAisUsersCount);
    }

    public UserDTO getUser(final UUID userId) {
        return userRepository.findById(userId)
                .map(userDTOMapper::map)
                .orElseThrow(() -> new UserNotFoundException("User with user id " + userId + " does not exist"));
    }

    private void sendOneOffAISUserPromotedWebhook(User user, ClientUserToken clientUserToken) {
        var userEventWebhookPayload = new UserEventWebhookPayload(
                UUID.randomUUID(),
                user.getUserId(),
                ONE_OFF_AIS_USER_PROMOTED_MESSAGE,
                UserEventWebhookPayload.Event.ONE_OFF_USER_PROMOTED_TO_RECURRING
        );

        webhookEventsProducer.sendWebhook(userEventWebhookPayload, clientUserToken);
    }

    public UserIdsDTO listUsers(UUID clientId, String next) {
        List<User> users;
        if (next != null) {
            String[] cursor = new String(Base64.decode(next)).split(",");
            ZonedDateTime fromCreated = ZonedDateTime.parse(cursor[0], formatter);
            UUID fromId = UUID.fromString(cursor[1]);
            users = userRepository.findAllByClientIdAndCreatedTimeAndId(clientId, fromCreated, fromId);
        } else {
            users = userRepository.findAllByClientId(clientId);
        }
        List<UUID> clientUserIds = users.stream()
                .map(User::getClientUserId)
                .collect(Collectors.toList());
        String newNext;
        if (users.size() == 1000) {
            User lastUser = users.get(users.size() - 1);
            newNext = Base64.toBase64String(String.format("%s,%s", formatter.format(lastUser.getCreated()), lastUser.getUserId()).getBytes(StandardCharsets.UTF_8));
        } else {
            newNext = null;
        }
        return new UserIdsDTO(clientUserIds, newNext);
    }

    public Optional<UserDTO> blockUser(ClientUserToken clientUserToken, BlockUserRequestDTO request) {
        final UUID userId = clientUserToken.getUserIdClaim();
        return userRepository.findUserByUserIdAndDeletedIsNull(userId)
                .map(cu -> blockUser(request, cu, clientUserToken))
                .map(userDTOMapper::map);
    }

    private User blockUser(BlockUserRequestDTO request, User user, ClientUserToken clientUserToken) {
        if (Boolean.TRUE.equals(request.getBlocked())) {
            user.setBlocked(Instant.now(clock));
            user.setBlockedReason(request.getReason());
            user.setBlockedBy(request.getRequestedBy());
        } else {
            user.setBlocked(null);
            user.setBlockedReason(null);
            user.setBlockedBy(null);
        }

        User savedUser = userRepository.save(user);
        log.info("Successfully updated the blocked status of client user: clientId={}, userId={}, clientUserId={}", clientUserToken.getClientIdClaim(), clientUserToken.getUserIdClaim(), clientUserToken.getClientUserIdClaim());
        sendBlockedWebhook(request, clientUserToken);
        return savedUser;
    }

    private void sendBlockedWebhook(BlockUserRequestDTO request, ClientUserToken clientUserToken) {
        final UserEventWebhookPayload payload = new UserEventWebhookPayload(
                UUID.randomUUID(),
                clientUserToken.getUserIdClaim(),
                request.getReason(),
                Boolean.TRUE.equals(request.getBlocked()) ? BLOCKED : UNBLOCKED);

        webhookEventsProducer.sendWebhook(payload, clientUserToken);
    }

    public Optional<UserDTO> getUserByClientUserId(UUID clientId, UUID clientUserId) {
        return userRepository.findByClientIdAndClientUserId(clientId, clientUserId)
                .map(userDTOMapper::map);
    }

    public void deleteUser(final ClientUserToken clientUserToken) {
        maintenanceClient.deleteUser(clientUserToken);
    }
}
