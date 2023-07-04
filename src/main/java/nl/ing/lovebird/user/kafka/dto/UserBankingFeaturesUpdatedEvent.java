package nl.ing.lovebird.user.kafka.dto;

import lombok.Value;

import java.util.UUID;

@Value
public class UserBankingFeaturesUpdatedEvent {

    UUID userId;
    String eventType;
}