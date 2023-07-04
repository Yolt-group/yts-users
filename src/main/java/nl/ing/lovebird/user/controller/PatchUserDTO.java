package nl.ing.lovebird.user.controller;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatchUserDTO {

    private boolean isOneOffAis;
}
