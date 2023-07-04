package nl.ing.lovebird.user.datascience;

import lombok.Getter;

import java.util.UUID;

@Getter
public class OffloadUserMessage<T extends UserData> {
    private final boolean delete = false;
    private final int schema_version;
    private final String entity_id;
    private final T payload;

    public OffloadUserMessage(UUID cliendId, UUID userId, int schema_version, T payload) {
        this.schema_version = schema_version;
        this.entity_id = cliendId + ":" + userId;
        this.payload = payload;
    }
}
