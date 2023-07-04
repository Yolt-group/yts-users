package nl.ing.lovebird.user.repository.postgres.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "client_user_id")
    private UUID clientUserId;

    @Column(name = "blocked")
    private Instant blocked;

    @Column(name = "blocked_reason")
    private String blockedReason;

    @Column(name = "blocked_by")
    private String blockedBy;

    @Column(name = "created")
    private Instant created;

    @Column(name = "one_off_ais")
    private boolean oneOffAis;

    @Column(name = "deleted")
    private Instant deleted;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }
}
