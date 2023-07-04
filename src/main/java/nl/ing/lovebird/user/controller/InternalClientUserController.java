package nl.ing.lovebird.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.annotations.VerifiedClientToken;
import nl.ing.lovebird.user.controller.dto.UserDTO;
import nl.ing.lovebird.user.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/internal/client-users")
@RequiredArgsConstructor
@Validated
public class InternalClientUserController {

    private final UserService userService;

    // Used by Cucumber
    @GetMapping(value = "/{clientUserId}", produces = APPLICATION_JSON_VALUE)
    public Optional<UserDTO> getClientUser(
            @VerifiedClientToken ClientToken clientToken,
            @PathVariable UUID clientUserId) {
        return userService.getUserByClientUserId(clientToken.getClientIdClaim(), clientUserId);
    }

    // Used by client-gateway
    @GetMapping(value = "/{clientId}/{clientUserId}", produces = APPLICATION_JSON_VALUE)
    public Optional<UserDTO> getClientUserMapping(
            @PathVariable final UUID clientId,
            @PathVariable UUID clientUserId) {
        return userService.getUserByClientUserId(clientId, clientUserId).filter(user -> user.getDeleted() == null);
    }
}
