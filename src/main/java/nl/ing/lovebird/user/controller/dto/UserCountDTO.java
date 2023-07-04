package nl.ing.lovebird.user.controller.dto;

import lombok.Value;

@Value
public class UserCountDTO {
    long count;
    long oneOffAisUsersCount;
}
