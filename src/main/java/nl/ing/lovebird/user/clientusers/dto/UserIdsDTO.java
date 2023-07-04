package nl.ing.lovebird.user.clientusers.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Schema(name = "UserIds")
public class UserIdsDTO {
    @ArraySchema(arraySchema = @Schema(description = "The list of users"))
    private List<UUID> userIds;
    @Schema(description = "The reference to the next page. This value should be provided by making a subsequent API call with this as query parameter next=${next}")
    private String next;
}
