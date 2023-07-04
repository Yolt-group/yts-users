package nl.ing.lovebird.user.clientusers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.annotations.VerifiedClientToken;
import nl.ing.lovebird.logging.MDCContextCreator;
import nl.ing.lovebird.user.clientusers.dto.AddClientUserDTO;
import nl.ing.lovebird.user.clientusers.dto.ExternalClientUserDTO;
import nl.ing.lovebird.user.controller.dto.UserDTO;
import nl.ing.lovebird.user.service.MaintenanceClient;
import nl.ing.lovebird.user.service.UserService;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/client-users")
@Tag(name = "client-users")
@RequiredArgsConstructor
@Deprecated
public class ClientUserController {

    private static final List<UUID> DEPRECATED_ENDPOINT_CLIENT_GROUP_WHITELIST = Arrays.asList(
            UUID.fromString("141f08f5-cc7a-483e-beeb-3e28244404b1"),
            UUID.fromString("131e3eb1-ae5c-41ce-82ab-d57a6e85175e"),
            UUID.fromString("921ba0d6-f78f-43ec-845b-ee15338deb0a") // Yolt DevOps
    );

    private final UserService userService;

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ExternalClientUserDTO> createClientUser(@Parameter(hidden = true) @VerifiedClientToken ClientToken clientToken,
                                                                  @RequestBody final AddClientUserDTO clientUserDTO) {
        if (!DEPRECATED_ENDPOINT_CLIENT_GROUP_WHITELIST.contains(clientToken.getClientGroupIdClaim())) {
            log.error("Returning 410 Gone on POST /client-users for client-group {}", clientToken.getClientGroupIdClaim());
            return ResponseEntity.status(HttpStatus.GONE).build();
        }

        try {
            MDC.put(MDCContextCreator.CLIENT_ID_HEADER_NAME, clientToken.getClientIdClaim().toString());

            UserDTO user = userService.createNewUser(clientToken, false);

            ExternalClientUserDTO response = new ExternalClientUserDTO(user.getClientUserId(), user.getClientId(), clientUserDTO.getCountryCode(), Date.from(user.getCreated().toInstant(ZoneOffset.UTC)));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } finally {
            MDC.remove(MDCContextCreator.CLIENT_ID_HEADER_NAME);
        }
    }

    @Operation(summary = "Delete a client user", responses = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the client user")
    })
    @DeleteMapping(value = "/me", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteMe(@Parameter(hidden = true) @VerifiedClientToken ClientUserToken clientUserToken) {
        userService.deleteUser(clientUserToken);
        return ResponseEntity.noContent().build();
    }
}
