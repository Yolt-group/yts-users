package nl.ing.lovebird.user.clientusers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(name = "AddClientUser", description = "Information you have to provide to create a client-user")
@Deprecated
public class AddClientUserDTO {

    @Schema(description = "The country code of the user's residence", required = true)
    private String countryCode;

}
