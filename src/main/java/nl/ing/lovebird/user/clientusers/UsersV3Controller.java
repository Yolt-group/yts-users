package nl.ing.lovebird.user.clientusers;

import io.swagger.v3.oas.annotations.Hidden;
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
@Validated
@RequestMapping("/v3/users")
@Deprecated // TODO - YCL-2929 cleanup v3 endpoint
public class UsersV3Controller {

    private final UserService userService;

    @DeleteMapping(value = "{userId}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Deprecated
    public void delete(@Parameter(hidden = true) @VerifiedClientToken ClientUserToken clientUserToken) {
        userService.deleteUser(clientUserToken);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @Deprecated
    public UserIdsDTO listClientUsers(@VerifiedClientToken ClientToken clientToken,
                                      @Nullable @RequestParam(required = false)
                                      @Size(max = 200) String next) {
        UUID clientId = clientToken.getClientIdClaim();
        return userService.listUsers(clientId, next);
    }
}
