package nl.ing.lovebird.user.repository.postgres;

import nl.ing.lovebird.user.controller.dto.UserDTO;
import nl.ing.lovebird.user.repository.postgres.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface UserRepository extends CrudRepository<User, UUID> {

    Optional<User> findById(UUID userId);

    Optional<User> findUserByUserIdAndDeletedIsNull(UUID userId);

    List<User> findAllByClientIdAndDeletedIsNull(UUID clientId);

    @Query(value = "SELECT * FROM users WHERE client_id = ?1 AND (created < ?2 OR (created <= ?2 AND user_id < ?3)) ORDER BY created DESC, user_id DESC LIMIT 1000", nativeQuery = true)
    List<User> findAllByClientIdAndCreatedTimeAndId(UUID clientId, ZonedDateTime fromCreated, UUID fromId);

    @Query(value = "SELECT * FROM users WHERE client_id = ?1 ORDER BY created DESC, user_id DESC LIMIT 1000", nativeQuery = true)
    List<User> findAllByClientId(UUID clientId);

    Optional<User> findByClientIdAndClientUserId(UUID clientId, UUID clientUserId);

    Stream<User> streamAllBy();
}
