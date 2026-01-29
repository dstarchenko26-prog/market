package ua.nulp.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Запит на реєстрацію нового користувача")
public class RegisterRequest {

    @Schema(
            description = "Ім'я користувача",
            example = "Denis",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "First name cannot be empty")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Schema(
            description = "Прізвище користувача",
            example = "Testov",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Last name cannot be empty")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Schema(
            description = "Електронна пошта (унікальна)",
            example = "denis@test.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(
            description = "Пароль (мінімум 8 символів, цифри та букви)",
            example = "StrongP@ssw0rd!",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}