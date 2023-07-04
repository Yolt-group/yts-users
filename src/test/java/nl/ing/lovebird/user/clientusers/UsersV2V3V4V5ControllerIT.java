package nl.ing.lovebird.user.clientusers;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.test.TestClientTokens;
import nl.ing.lovebird.user.BaseIntegrationTest;
import nl.ing.lovebird.user.repository.postgres.UserRepository;
import nl.ing.lovebird.user.repository.postgres.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class UsersV2V3V4V5ControllerIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TestClientTokens testClientTokens;

    @Autowired
    Clock clock;

    final UUID clientGroupId = UUID.randomUUID();
    final UUID clientId = UUID.randomUUID();
    ClientToken clientToken;

    @BeforeEach
    void before() {
        clientToken = testClientTokens.createClientToken(clientGroupId, clientId);
    }

    @Test
    void getUsers() throws Exception {
        IntStream.range(0, 1001).forEach(i -> userRepository.save(new User(UUID.randomUUID(), clientId, UUID.randomUUID(), null, null, null, LocalDateTime.now(clock).toInstant(ZoneOffset.UTC), false, null)));

        { // v2
            MvcResult result = mockMvc.perform(get("/v2/users")
                            .header("client-token", clientToken.getSerialized()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userIds", hasSize(1000)))
                    .andExpect(jsonPath("$.next", notNullValue()))
                    .andReturn();

            String next = JsonPath.read(result.getResponse().getContentAsString(), "$.next");

            mockMvc.perform(get("/v2/users?next=" + next)
                            .header("client-token", clientToken.getSerialized()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userIds", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.next", nullValue()))
                    .andReturn();
        }
        { // v3
            MvcResult result = mockMvc.perform(get("/v3/users")
                            .header("client-token", clientToken.getSerialized()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userIds", hasSize(1000)))
                    .andExpect(jsonPath("$.next", notNullValue()))
                    .andReturn();

            String next = JsonPath.read(result.getResponse().getContentAsString(), "$.next");

            mockMvc.perform(get("/v3/users?next=" + next)
                            .header("client-token", clientToken.getSerialized()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userIds", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.next", nullValue()))
                    .andReturn();
        }
        { // v4
            MvcResult result = mockMvc.perform(get("/v4/users")
                            .header("client-token", clientToken.getSerialized()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userIds", hasSize(1000)))
                    .andExpect(jsonPath("$.next", notNullValue()))
                    .andReturn();

            String next = JsonPath.read(result.getResponse().getContentAsString(), "$.next");

            mockMvc.perform(get("/v4/users?next=" + next)
                            .header("client-token", clientToken.getSerialized()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userIds", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.next", nullValue()))
                    .andReturn();
        }
        { // v5
            MvcResult result = mockMvc.perform(get("/v5/users")
                            .header("client-token", clientToken.getSerialized()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userIds", hasSize(1000)))
                    .andExpect(jsonPath("$.next", notNullValue()))
                    .andReturn();

            String next = JsonPath.read(result.getResponse().getContentAsString(), "$.next");

            mockMvc.perform(get("/v5/users?next=" + next)
                            .header("client-token", clientToken.getSerialized()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userIds", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$.next", nullValue()))
                    .andReturn();
        }
    }
}
