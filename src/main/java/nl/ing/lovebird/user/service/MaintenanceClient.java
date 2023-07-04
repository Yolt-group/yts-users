package nl.ing.lovebird.user.service;

import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.UUID;

@Service
public class MaintenanceClient {

    private final RestTemplate restTemplate;

    public MaintenanceClient(final RestTemplateBuilder restTemplateBuilder,
                             @Value("${service.maintenance.timeout-in-seconds:5}") final Integer maintenanceServiceTimeout,
                             @Value("${service.maintenance.url:https://maintenance/maintenance}") final String maintenanceServiceUrl
    ) {
        restTemplate = restTemplateBuilder.rootUri(maintenanceServiceUrl)
                .setConnectTimeout(Duration.ofSeconds(maintenanceServiceTimeout))
                .setReadTimeout(Duration.ofSeconds(maintenanceServiceTimeout))
                .build();
    }

    /**
     * Deletes given user ID from the Yolt system.
     *
     * @param clientUserToken the client user token
     */
    public void deleteUser(final ClientUserToken clientUserToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, clientUserToken.getSerialized());
        restTemplate.exchange("/delete-user/{userId}", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class, clientUserToken.getUserIdClaim());
    }
}
