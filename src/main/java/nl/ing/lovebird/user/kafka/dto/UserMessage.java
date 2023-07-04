package nl.ing.lovebird.user.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import nl.ing.lovebird.user.repository.domain.UserStatusType;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"phoneNumber", "firstName", "lastName", "emailAddress"})
public class UserMessage {

    private UUID id;
    private UserStatusType status;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String preferredCurrency;
    private String countryCode;
    private boolean refreshAccountsOnLogin;
    private ZonedDateTime created;
    private String trackingId;
    private UUID clientId;
    private ZonedDateTime lastLogin;
    private Locale locale;
    private LocalDate kycGracePeriodEndDate;
    private Set<String> enabledFeatures;
    private boolean yolt2User;
    private boolean bankingEnrollmentCompleted;
    private boolean hasVerifiedEmail;
    private boolean phoneNumberClaimed;
    private boolean cardActivated;
    private UUID businessPartnerId;
    private boolean oneOffAis;
}
