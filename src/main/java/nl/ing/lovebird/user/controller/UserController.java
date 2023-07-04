package nl.ing.lovebird.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.annotations.VerifiedClientToken;
import nl.ing.lovebird.user.controller.dto.BlockUserRequestDTO;
import nl.ing.lovebird.user.controller.dto.CreateUserDTO;
import nl.ing.lovebird.user.controller.dto.UserCountDTO;
import nl.ing.lovebird.user.controller.dto.UserDTO;
import nl.ing.lovebird.user.service.UserService;
import nl.ing.lovebird.user.service.domain.CreateUserCommand;
import nl.ing.lovebird.user.service.domain.UpdateUserCommand;
import org.apache.commons.lang3.NotImplementedException;
import org.jboss.logging.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

import static nl.ing.lovebird.logging.MDCContextCreator.USER_ID_HEADER_NAME;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping(value = "deprecated", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createDeprecatedUser(
            @VerifiedClientToken ClientToken clientToken,
            @RequestBody final @Valid CreateUserDTO userDTO
    ) {
        final CreateUserCommand userToBeCreated = CreateUserCommand.builder()
                .clientId(clientToken.getClientIdClaim())
                .oneOffAis(userDTO.isOneOffAis())
                .build();

        return userService.createNewDeprecatedUser(clientToken, userToBeCreated);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(
            @VerifiedClientToken ClientToken clientToken,
            @RequestBody final @Valid CreateUserDTO userDTO
    ) {
        if (userDTO.isOneOffAis() && !clientToken.hasOneOffAIS()) {
            throw new IllegalArgumentException("Cannot create an one off AIS user for a client that does not have this feature");
        }

        return userService.createNewUser(clientToken, userDTO.isOneOffAis());
    }

    @PatchMapping(value = "{userId}", produces = APPLICATION_JSON_VALUE)
    public void updateUserInternal(@PathVariable("userId") final UUID userId,
                                   @RequestBody @Valid PatchUserDTO patchUserDTO,
                                   @VerifiedClientToken ClientUserToken clientUserToken) {
        MDC.put(USER_ID_HEADER_NAME, userId);
        log.info("Received internal user update");

        final var updateUserCommand = UpdateUserCommand.builder()
                .userId(userId)
                .oneOffAis(patchUserDTO.isOneOffAis())
                .build();

        userService.updateUser(clientUserToken, updateUserCommand);
        MDC.remove(USER_ID_HEADER_NAME);
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public UserDTO getUserByUserId(@PathVariable final UUID id) {
        return userService.getUser(id);
    }

    @Operation(summary = "Block or unblock a client user", responses = {
            @ApiResponse(responseCode = "200", description = "Successfully (un)blocked client user")
    })
    @PostMapping(value = "/block", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> blockClientUser(
            @VerifiedClientToken ClientUserToken clientUserToken,
            @RequestBody @Valid final BlockUserRequestDTO request) {
        return userService.blockUser(clientUserToken, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Client user not found: clientId={}, userId={}", clientUserToken.getClientIdClaim(), clientUserToken.getUserIdClaim());
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, params = "count-by")
    public ResponseEntity<UserCountDTO> countUsers(
            @RequestParam(value = "count-by", defaultValue = "CLIENT") CountBy countBy,
            @RequestParam(value = "client-id", required = false) UUID clientId
    ) {
        if (countBy == CountBy.CLIENT) {
            if (clientId == null) {
                return ResponseEntity.badRequest().build();
            }
            var userCountDTO = userService.countByClientId(clientId);
            return ResponseEntity.ok(userCountDTO);
        }

        throw new NotImplementedException("Unsupported search by type: " + countBy);
    }

    private enum CountBy {
        CLIENT
    }
}
