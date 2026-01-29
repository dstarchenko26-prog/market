package ua.nulp.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запит на автентифікацію (вхід в систему)")
public class AuthRequest {

    @Schema(
            description = "Електронна пошта користувача",
            example = "test@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(
            description = "Пароль користувача",
            example = "StrongP@ssw0rd!",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password cannot be empty")
    private String password;
}