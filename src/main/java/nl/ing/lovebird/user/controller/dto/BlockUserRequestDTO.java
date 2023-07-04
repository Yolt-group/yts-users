package nl.ing.lovebird.user.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockUserRequestDTO {

    private static final String ALLOWED_CHARS = "^[A-Za-z0-9!@#$%&()\\-_=+;:'\",./? ]+$";

    @NotNull
    private Boolean blocked;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = ALLOWED_CHARS)
    private String requestedBy;

    @NotBlank
    @Size(max = 250)
    @Pattern(regexp = ALLOWED_CHARS)
    private String reason;

}
