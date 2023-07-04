package nl.ing.lovebird.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.test.TestClientTokens;
import nl.ing.lovebird.user.controller.dto.UserDTO;
import nl.ing.lovebird.user.BaseIntegrationTest;
import nl.ing.lovebird.user.repository.postgres.UserRepository;
import nl.ing.lovebird.user.repository.postgres.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserTestConsumer userTestConsumer;
    @Autowired
    private OffloadUserTestConsumer offloadUserTestConsumer;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Clock clock;
    @Autowired
    private TestClientTokens testClientTokens;

    @BeforeEach
    void beforeEach() {
        userTestConsumer.clear();
    }

    @Test
    void shouldCreateUser_whenCreateUser_givenValidRequest() throws Exception {
        var clientGroupId = UUID.randomUUID();
        var clientId = UUID.randomUUID();
        var userDTO = UserDTO.builder()
                .clientId(clientId)
                .build();

        var clientToken = testClientTokens.createClientToken(clientGroupId, clientId);
        var mvcResult = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDTO))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("client-token", clientToken.getSerialized()))
                .andExpect(status().isCreated())
                .andReturn();

        var createdUser = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), UserDTO.class);
        assertThat(createdUser)
                .usingRecursiveComparison()
                .ignoringFields("id", "userId", "clientUserId", "created")
                .isEqualTo(userDTO);
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getClientId()).isEqualTo(clientId);
        assertThat(createdUser.getClientUserId()).isNotNull();
        assertThat(createdUser.getCreated()).isCloseTo(LocalDateTime.now(clock), within(1, ChronoUnit.HOURS));

        assertThat(userRepository.findById(createdUser.getId())).isPresent();

        // Test the kafka event was sent
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            var kafkaEvents = userTestConsumer.getUserReceiveds();
            assertThat(kafkaEvents).hasSize(1);
            var event = kafkaEvents.get(0);
            assertThat(event.getValue().getHeaders().getMessageType()).isEqualTo("USER_CREATED");
            assertThat(event.getClientToken()).isExactlyInstanceOf(ClientToken.class);

            List<String> offloadedKeys = offloadUserTestConsumer.getKeys();
            assertThat(offloadedKeys).contains(clientId + ":" + createdUser.getUserId());
        });
    }

    @Test
    void shouldPromoteOneOfAisUser_whenUpdateUser_givenValidRequest() throws Exception {
        var clientGroupId = UUID.randomUUID();
        var clientId = UUID.randomUUID();
        var clientToken = testClientTokens.createClientToken(clientGroupId, clientId, (claims) -> {
            claims.setClaim("one_off_ais", true);
        });

        var userDTO = UserDTO.builder()
                .oneOffAis(true)
                .clientId(clientId)
                .build();
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDTO))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("client-token", clientToken.getSerialized()))
                .andExpect(status().isCreated());

        var userCreated = await().atMost(Duration.ofSeconds(5)).until(() -> {
            var kafkaEvents = userTestConsumer.getUserReceiveds();
            assertThat(kafkaEvents).hasSize(1);
            return kafkaEvents.get(0).getValue();
        }, Objects::nonNull);

        var userId = userCreated.getPayload().getId();
        var clientUserToken = testClientTokens.createClientUserToken(clientGroupId, clientId, userId);

        var patchUserDTO = PatchUserDTO.builder()
                .isOneOffAis(false)
                .build();
        mockMvc.perform(patch("/users/{userId}", userId)
                        .content(objectMapper.writeValueAsString(patchUserDTO))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("client-token", clientUserToken.getSerialized()))
                .andExpect(status().isOk());

        // Test the kafka event was sent
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            var kafkaEvents = userTestConsumer.getUserReceiveds();
            assertThat(kafkaEvents).hasSize(2);
            var event = kafkaEvents.get(1);
            var userUpdateDTO = event.getValue();
            assertThat(userUpdateDTO.getHeaders().getMessageType()).isEqualTo("USER_UPDATED");
            assertThat(event.getClientToken()).isExactlyInstanceOf(ClientUserToken.class);
            assertThat(userUpdateDTO.getPayload().isOneOffAis()).isFalse();
        });
    }

    @Test
    void shouldReturnUserDTO_whenGetUser_givenClientUserExists() throws Exception {
        UUID clientId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID clientUserId = UUID.randomUUID();
        User user = new User(userId, clientId, clientUserId, null, null, null, Instant.now(clock), true, Instant.now(clock));
        userRepository.save(user);

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.clientId", is(clientId.toString())))
                .andExpect(jsonPath("$.clientUserId", is(clientUserId.toString())))
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.oneOffAis", is(true)));
    }

    @Test
    void shouldReturn404_whenGetUser_givenUserNotExists() throws Exception {
        mockMvc.perform(get("/users/{userId}", UUID.randomUUID()))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }
}
