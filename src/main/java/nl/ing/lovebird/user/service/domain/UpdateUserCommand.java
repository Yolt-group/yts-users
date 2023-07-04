package nl.ing.lovebird.user.service.domain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class
UpdateUserCommand {
    private UUID userId;
    private UUID clientId;
    private boolean oneOffAis;
}