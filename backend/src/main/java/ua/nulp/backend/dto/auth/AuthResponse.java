package ua.nulp.backend.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Відповідь сервера з токенами доступу")
public class AuthResponse {

    @Schema(
            description = "JWT Access Token (використовується для авторизації запитів)",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI...",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(
            description = "Refresh Token (використовується для отримання нового Access Token, коли старий сплив)",
            example = "d010787e-97c7-4979-a782-019672046467",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("refresh_token")
    private String refreshToken;
}