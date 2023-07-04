package nl.ing.lovebird.user.clientusers.dto;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Value
@Deprecated
public class ExternalClientUserDTO {

    private final UUID id;
    private final UUID clientId;
    private final String countryCode;
    private final Date created;

}
