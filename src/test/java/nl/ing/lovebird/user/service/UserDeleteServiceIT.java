package nl.ing.lovebird.user.service;

import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.test.TestClientTokens;
import nl.ing.lovebird.user.BaseIntegrationTest;
import nl.ing.lovebird.user.repository.postgres.UserRepository;
import nl.ing.lovebird.user.repository.postgres.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserDeleteServiceIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Clock clock;

    @Autowired
    private TestClientTokens testClientTokens;

    final UUID userId = UUID.randomUUID();
    final UUID clientGroupId = UUID.randomUUID();
    final UUID clientId = UUID.randomUUID();

    @Test
    void testDeleteUserNotFound200() throws Exception {
        ClientUserToken token = testClientTokens.createClientUserToken(clientGroupId, clientId, userId);
        mockMvc.perform(delete("/delete-user/{id}", userId)
                        .header("client-token", token.getSerialized()))
                .andExpect(status().is2xxSuccessful());

        wireMockServer.verify(0, deleteRequestedFor(urlEqualTo("/maintenance/delete-user/" + userId))
            .withHeader("client-token", equalTo("client-user-token-value")));
    }

    @Test
    void testDeleteUser() throws Exception {
        ClientUserToken token = testClientTokens.createClientUserToken(clientGroupId, clientId, userId);

        userRepository.save(new User(userId, clientId, UUID.randomUUID(), null, null, null, Instant.now(clock), false, null));

        mockMvc.perform(delete("/delete-user/{id}", userId)
                        .header("client-token", token.getSerialized()))
                .andExpect(status().is2xxSuccessful());

        wireMockServer.verify(0, deleteRequestedFor(urlEqualTo("/maintenance/delete-user/" + userId)));

        Optional<User> optionalUser = userRepository.findById(userId);
        assertThat(optionalUser).isPresent();

        User expected = optionalUser.get();
        assertThat(expected.getDeleted()).isNotNull();
    }

}
