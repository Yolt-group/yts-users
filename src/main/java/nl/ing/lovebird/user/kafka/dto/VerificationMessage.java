package nl.ing.lovebird.user.kafka.dto;

import lombok.Value;

import java.util.UUID;

@Value
public class VerificationMessage {

    UUID userId;
    Channel channel;
    Type type;
    String address;
    UUID changeId;
    String code;

    public enum Channel {
        EMAIL,
        SMS
    }

    public enum Type {
        VERIFICATION
    }
}
