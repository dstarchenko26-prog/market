package ua.nulp.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nulp.backend.service.AuthService;
import ua.nulp.backend.dto.auth.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Керування автентифікацією, реєстрацією та безпекою користувачів")
public class AuthController {

    private final AuthService authService;

    // =================================================================================
    // REGISTER
    // =================================================================================
    @Operation(summary = "Реєстрація нового користувача", description = "Створює нового користувача в статусі PENDING та надсилає лист підтвердження.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Лист підтвердження успішно надіслано"),
            @ApiResponse(responseCode = "400", description = "Помилка валідації даних", content = @Content),
            @ApiResponse(responseCode = "409", description = "Користувач з таким email вже існує", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("A mail confirmation has been sent.");
    }

    // =================================================================================
    // LOGIN
    // =================================================================================
    @Operation(summary = "Вхід в систему (Login)", description = "Автентифікація за поштою та паролем. Повертає Access та Refresh токени. Також виконує злиття кошика гостя з акаунтом.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успішний вхід"),
            @ApiResponse(responseCode = "400", description = "Акаунт заблоковано або не активовано", content = @Content),
            @ApiResponse(responseCode = "401", description = "Невірний логін або пароль", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid AuthRequest request,

            @Parameter(description = "ID сесії гостя (для перенесення кошика і аналізу дій)", example = "uuid-session-id")
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,

            @Parameter(description = "Інформація про пристрій (автоматично від браузера)", hidden = true)
            @RequestHeader(value = HttpHeaders.USER_AGENT, required = false, defaultValue = "Unknown") String userAgent
    ) {
        return ResponseEntity.ok(authService.login(request, sessionId, userAgent));
    }

    // =================================================================================
    // REFRESH TOKEN
    // =================================================================================
    @Operation(summary = "Оновлення Access токена", description = "Використовує Refresh Token для отримання нової пари токенів.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токени успішно оновлено"),
            @ApiResponse(responseCode = "401", description = "Refresh Token невірний або прострочений (потрібен ре-логін)", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid TokenRequest token) {
        return ResponseEntity.ok(authService.relogin(token));
    }

    // =================================================================================
    // CONFIRMATION (Email)
    // =================================================================================
    @Operation(summary = "Повторна відправка листа підтвердження", description = "Якщо попередній лист загубився або токен прострочився.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Лист надіслано"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено", content = @Content),
            @ApiResponse(responseCode = "400", description = "Акаунт вже активовано", content = @Content)
    })
    @PostMapping("/send-confirm")
    public ResponseEntity<String> resendConfirm(@RequestBody @Valid EmailRequest request) {
        authService.resendConfirm(request);
        return ResponseEntity.ok("A mail confirmation has been resent.");
    }

    @Operation(summary = "Підтвердження пошти", description = "Активує акаунт за токеном з листа та автоматично логінить користувача.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пошту підтверджено, повертаються токени"),
            @ApiResponse(responseCode = "400", description = "Токен невалідний, прострочений або вже використаний", content = @Content),
            @ApiResponse(responseCode = "404", description = "Токен не знайдено", content = @Content)
    })
    @PostMapping("/confirm")
    public ResponseEntity<AuthResponse> confirmEmail(
            @RequestBody @Valid TokenRequest token,

            @Parameter(description = "ID сесії гостя", example = "uuid-session-id")
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,

            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.USER_AGENT, required = false, defaultValue = "Unknown") String userAgent
    ) {
        return ResponseEntity.ok(authService.confirm(token, sessionId, userAgent));
    }

    // =================================================================================
    // MAGIC LINK
    // =================================================================================
    @Operation(summary = "Вхід без паролю (запит)", description = "Відправляє магічне посилання для входу на пошту.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Лист надіслано"),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено", content = @Content),
            @ApiResponse(responseCode = "400", description = "Акаунт заблоковано або не активовано", content = @Content)
    })
    @PostMapping("/send-link-login")
    public ResponseEntity<String> sendLinkLogin(@RequestBody @Valid EmailRequest email) {
        authService.sendLinkLogin(email);
        return ResponseEntity.ok("Entry letter sent.");
    }

    @Operation(summary = "Вхід без паролю (підтвердження)", description = "Обмінює токен з листа на JWT токени.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успішний вхід"),
            @ApiResponse(responseCode = "400", description = "Токен прострочений або невалідний", content = @Content)
    })
    @PostMapping("/link-login")
    public ResponseEntity<AuthResponse> linkLogin(
            @RequestBody @Valid TokenRequest token,

            @Parameter(description = "ID сесії гостя", example = "uuid-session-id")
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId,

            @Parameter(hidden = true)
            @RequestHeader(value = HttpHeaders.USER_AGENT, required = false, defaultValue = "Unknown") String userAgent
    ) {
        return ResponseEntity.ok(authService.linkLogin(token, sessionId, userAgent));
    }

    // =================================================================================
    // PASSWORD RESET
    // =================================================================================
    @Operation(summary = "Запит на скидання пароля", description = "Відправляє лист з посиланням на відновлення пароля.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Інструкції надіслано (навіть якщо email не існує, задля безпеки)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Користувача не знайдено (якщо ви вирішили повідомляти про це)", content = @Content)
    })
    @PostMapping("/reset-password")
    public ResponseEntity<String> sendResetPassword(@RequestBody @Valid EmailRequest email) {
        authService.sendResetPassword(email);
        return ResponseEntity.ok("Password reset email sent.");
    }

    @Operation(summary = "Встановлення нового пароля", description = "Змінює пароль користувача за наявності валідного токена.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пароль успішно змінено"),
            @ApiResponse(responseCode = "400", description = "Токен невалідний або пароль занадто простий", content = @Content)
    })
    @PatchMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}