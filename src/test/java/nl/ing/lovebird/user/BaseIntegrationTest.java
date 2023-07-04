package nl.ing.lovebird.user;

import com.github.tomakehurst.wiremock.WireMockServer;
import nl.ing.lovebird.postgres.test.EnableExternalPostgresTestDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@SpringBootTest(classes = {UserApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
@EnableExternalPostgresTestDatabase
@EmbeddedKafka(partitions = 1, topics = {"users", "allUsersBatch", "offloadUsers", "webhooks"})
@TestPropertySource(properties = {"environment=test"})
public abstract class BaseIntegrationTest {
    @Autowired
    protected WireMockServer wireMockServer;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @BeforeEach
    void beforeEachBase() {
        wireMockServer.resetAll();

        kafkaListenerEndpointRegistry.getListenerContainers()
                .forEach(messageListenerContainer -> ContainerTestUtils.waitForAssignment(messageListenerContainer, 1));
    }
}
