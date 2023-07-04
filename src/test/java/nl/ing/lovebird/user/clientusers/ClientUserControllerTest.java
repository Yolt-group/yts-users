package nl.ing.lovebird.user.clientusers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.verification.ClientIdVerificationService;
import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import nl.ing.lovebird.clienttokens.verification.ClientUserIdVerificationService;
import nl.ing.lovebird.clienttokens.verification.UserIdVerificationService;
import nl.ing.lovebird.clienttokens.web.VerifiedClientTokenParameterResolver;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import nl.ing.lovebird.errorhandling.config.BaseExceptionHandlers;
import nl.ing.lovebird.user.clientusers.dto.AddClientUserDTO;
import nl.ing.lovebird.user.controller.ExceptionHandlers;
import nl.ing.lovebird.user.controller.dto.UserDTO;
import nl.ing.lovebird.user.service.MaintenanceClient;
import nl.ing.lovebird.user.service.UserService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ClientUserControllerTest {

    private static final UUID YOLT_TEST_GROUP_ID = UUID.fromString("921ba0d6-f78f-43ec-845b-ee15338deb0a");

    private MockMvc mockMvc;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private UserService userService;

    @Mock
    private ClientTokenParser clientTokenParser;


    @BeforeEach
    public void setup() {
        ClientUserController userController = new ClientUserController(userService);
        ExceptionHandlingService exceptionHandlingService = new ExceptionHandlingService("CU");
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new VerifiedClientTokenParameterResolver(clientTokenParser, new ClientIdVerificationService(), new ClientUserIdVerificationService(), new UserIdVerificationService()))
                .setControllerAdvice(new ExceptionHandlers(exceptionHandlingService), new BaseExceptionHandlers(exceptionHandlingService)).build();
    }

    @AfterEach
    public void after() {
        verifyNoMoreInteractions(userService);
    }

    @Test
    void shouldSuccessfullyCreateNewUser() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID clientUserId = UUID.randomUUID();
        long creationMillis = 1568271345803L;
        UserDTO user = new UserDTO(userId,
                clientId,
                clientUserId,
                userId,
                false,
                null,
                null,
                null,
                false,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(creationMillis), ZoneOffset.UTC),
                null);
        ClientToken clientToken = createClientTokenFor(clientId, YOLT_TEST_GROUP_ID);
        when(userService.createNewUser(clientToken, false)).thenReturn(user);
        userService.createNewUser(clientToken, false);
        when(clientTokenParser.parseClientToken(any())).thenReturn(clientToken);

        final String body = OBJECT_MAPPER.writeValueAsString(new AddClientUserDTO("FR"));
        HttpHeaders headers = new HttpHeaders();
        headers.put("client-token", Collections.singletonList("client-token-value"));

        this.mockMvc.perform(post("/client-users")
                .content(body)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.CREATED.value()))
                .andExpect(content().json("{\"id\":\"" + clientUserId + "\"," +
                        "\"countryCode\":\"FR\"," +
                        "\"created\":" + creationMillis + "," +
                        "\"clientId\":\"" + clientId + "\"}"));
    }

    @Test
    void shouldFailToCreateNewUserForNonWhitelistedClientGroup() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID randomClientGroupId = UUID.randomUUID();
        ClientToken clientToken = createClientTokenFor(clientId, randomClientGroupId);
        when(clientTokenParser.parseClientToken(any())).thenReturn(clientToken);

        final String body = OBJECT_MAPPER.writeValueAsString(new AddClientUserDTO("FR"));
        HttpHeaders headers = new HttpHeaders();
        headers.put("client-token", Collections.singletonList("client-token-value"));

        this.mockMvc.perform(post("/client-users")
                .content(body)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.GONE.value()));
    }

    @Test
    void shouldDeleteAClientUser() throws Exception {
        UUID randomUUID = UUID.randomUUID();

        HttpHeaders headers = new HttpHeaders();
        headers.put("client-token", List.of("client-token-value" + randomUUID));
        ClientUserToken clientUserToken = mock(ClientUserToken.class);
        when(clientTokenParser.parseClientToken("client-token-value" + randomUUID)).thenReturn(clientUserToken);

        this.mockMvc.perform(delete("/client-users/me")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()));

        verify(userService).deleteUser(clientUserToken);
    }

    private ClientToken createClientTokenFor(UUID clientId, UUID clientGroupId) {
        JwtClaims claims = new JwtClaims();
        claims.setClaim("sub", "client:" + clientId.toString());
        claims.setClaim("client-id", clientId.toString());
        claims.setClaim("client-group-id", clientGroupId.toString());
        claims.setClaim("isf", "site-management");
        String serialized = Base64.encodeBase64String(String.format("fake-client-token-for-%s", clientId.toString()).getBytes());
        return new ClientToken(serialized, claims);
    }
}
