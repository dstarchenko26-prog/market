package ua.nulp.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Універсальний запит для передачі токена (Refresh, Confirm, Magic Link)")
public class TokenRequest {

    @Schema(
            description = "Значення токена (UUID або JWT)",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Token cannot be empty")
    private String token;
}