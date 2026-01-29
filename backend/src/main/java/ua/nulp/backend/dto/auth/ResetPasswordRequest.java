package ua.nulp.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запит на встановлення нового пароля")
public class ResetPasswordRequest {

    @Schema(
            description = "Токен відновлення (з URL або листа)",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Token cannot be empty")
    private String token;

    @Schema(
            description = "Новий пароль користувача",
            example = "NewStrongP@ssw0rd!",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}