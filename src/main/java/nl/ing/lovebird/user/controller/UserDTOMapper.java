package nl.ing.lovebird.user.controller;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.user.controller.dto.UserDTO;
import nl.ing.lovebird.user.repository.postgres.model.User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class UserDTOMapper {

    public UserDTO map(final User user) {
        return new UserDTO(
                user.getUserId(),
                user.getClientId(),
                user.getClientUserId(),
                user.getUserId(),
                user.getBlocked() != null,
                null,
                user.getBlockedReason(),
                getLocalDateTime(user.getBlocked()),
                user.isOneOffAis(),
                getLocalDateTime(user.getCreated()),
                getLocalDateTime(user.getDeleted())
        );
    }

    public LocalDateTime getLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.of("UTC")) : null;
    }
}
