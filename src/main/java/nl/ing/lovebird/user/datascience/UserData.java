package nl.ing.lovebird.user.datascience;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Value
@AllArgsConstructor
public class UserData {
    @NotNull
    UUID clientId;
    @NotNull
    UUID userId;
    UUID clientUserId;
    Instant blocked;
    String blockedReason;
    @NotNull
    Instant created;
    @NotNull
    boolean oneOffAisUser;
    Instant deleted;
}
