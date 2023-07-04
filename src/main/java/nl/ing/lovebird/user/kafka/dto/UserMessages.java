package nl.ing.lovebird.user.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserMessages {

    private final List<UserMessage> users;
}
