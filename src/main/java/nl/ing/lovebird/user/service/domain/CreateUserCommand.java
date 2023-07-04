package nl.ing.lovebird.user.service.domain;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateUserCommand {

    private UUID clientId;
    private boolean oneOffAis;

}
