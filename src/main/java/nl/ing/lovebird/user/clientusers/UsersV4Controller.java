package nl.ing.lovebird.user.clientusers;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.annotations.VerifiedClientToken;
import nl.ing.lovebird.user.clientusers.dto.UserIdsDTO;
import nl.ing.lovebird.user.service.MaintenanceClient;
import nl.ing.lovebird.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Size;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The successor of ClientUserController which offers versioned endpoints for creating client-users.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v4/users")
@Validated
@Deprecated
public class UsersV4Controller {

    private final UserService userService;

    @DeleteMapping(value = "{userId}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(hidden = true) @VerifiedClientToken ClientUserToken clientUserToken) {
        userService.deleteUser(clientUserToken);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public UserIdsDTO listClientUsers(@Parameter(hidden = true) @VerifiedClientToken ClientToken clientToken,
                                      @Parameter(description = "Used for pagination. " +
                                              "If an initial call to this endpoint returns an object where the field 'next' is not null, then not all users were returned and more can be fetched. " +
                                              "To fetch more users, call this endpoint again and set this query parameter to the value of the field 'next' that was previously returned. " +
                                              "Repeat until the field next contains null.")
                                      @Nullable @RequestParam(required = false)
                                      @Size(max = 200) String next) {
        UUID clientId = clientToken.getClientIdClaim();
        return userService.listUsers(clientId, next);
    }
}
