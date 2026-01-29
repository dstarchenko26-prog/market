package ua.nulp.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.nulp.backend.dto.auth.*;
import ua.nulp.backend.entity.enums.UserRole;
import ua.nulp.backend.entity.enums.UserStatus;
import ua.nulp.backend.entity.user.token.*;
import ua.nulp.backend.entity.user.user.User;
import ua.nulp.backend.entity.user.user.UserSettings;
import ua.nulp.backend.exception.*;
import ua.nulp.backend.repository.*;
import ua.nulp.backend.security.JwtService;
import ua_parser.Client;
import ua_parser.Parser;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final ConfirmationEmailTokenRepository confirmationEmailTokenRepository;
    private final MagicLoginTokenRepository magicLoginTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;

    private final JwtService jwtService;
    private final EmailService emailService;
    private final SynchronizeService synchronizeService;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final Parser uaParser;

    @Value("${frontend.full-url}")
    private String fullUrl;
    @Value("${app.path.confirm-email}")
    private String pathToConfirm;
    @Value("${app.path.link-login}")
    private String pathToLink;
    @Value("${app.path.reset-password}")
    private String pathToReset;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    // =================================================================================
    // 1. REGISTER
    // =================================================================================
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceConflictException("User with this email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.PENDING)
                .role(UserRole.BUYER)
                .failedLoginAttempts(0)
                .lastSeenAt(Instant.now())
                .build();

        UserSettings settings = UserSettings.builder()
                .user(user)
                .language("UK")
                .theme("LIGHT")
                .emailNotification(true)
                .build();
        user.setUserSettings(settings);

        User savedUser = userRepository.save(user);

        createAndSendConfirmationToken(savedUser);
    }

    // =================================================================================
    // 2. LOGIN
    // =================================================================================
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public AuthResponse login(AuthRequest request, String sessionId, String userAgent) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            if (user.getFailedLoginAttempts() > 0) {
                user.resetFailedAttempts();
                userRepository.save(user);
            }

            //updateLastSeenAsync(user.getId());

        } catch (BadCredentialsException e) {
            handleFailedLogin(user);
            throw e;
        } catch (DisabledException e) {
            throw new BadRequestException("Account is not active. Please verify your email.");
        } catch (LockedException e) {
            throw new BadRequestException("Account is temporarily locked due to suspicious activity.");
        }

        if (sessionId != null) {
            try {
//                synchronizeService.syncCarts(sessionId, user);
            } catch (Exception e) {}
        }

        return generateTokens(user, userAgent);
    }

    // =================================================================================
    // 3. REFRESH TOKEN (Детальна реалізація)
    // =================================================================================
    @Transactional
    public AuthResponse relogin(TokenRequest request) {
        String requestRefreshToken = request.getToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(token -> {
                    if (token.getExpiredAt().isBefore(Instant.now())) {
                        refreshTokenRepository.delete(token);
                        throw new BadRequestException("Refresh token expired. Please login again.");
                    }
                    return token;
                })
                .map(token -> {
                    User user = token.getUser();
                    //updateLastSeenAsync(user.getId());
                    String newAccessToken = jwtService.generateToken(user);
                    return AuthResponse.builder()
                            .accessToken(newAccessToken)
                            .refreshToken(token.getToken())
                            .build();
                })
                .orElseThrow(() -> new BadRequestException("Refresh token not found!"));
}

    // =================================================================================
    // 4. CONFIRM EMAIL
    // =================================================================================
    @Transactional
    public AuthResponse confirm(TokenRequest request, String sessionId, String userAgent) {
        ConfirmationEmailToken token = confirmationEmailTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));

        if (!token.isValid()) {
            throw new BadRequestException("Link expired or already used. Please request a new one.");
        }

        User user = token.getUser();

        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
        }

        token.setUsedAt(Instant.now());
        confirmationEmailTokenRepository.save(token);

        if (sessionId != null) {
//            synchronizeService.syncCarts(sessionId, user);
        }

        return generateTokens(user, userAgent);
    }

    // =================================================================================
    // 5. RESEND CONFIRM
    // =================================================================================
    @Transactional
    public void resendConfirm(EmailRequest email) {
        User user = userRepository.findByEmail(email.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BadRequestException("Account is already verified");
        }

        confirmationEmailTokenRepository.deleteAllByUser(user);

        createAndSendConfirmationToken(user);
    }

    // =================================================================================
    // 6. MAGIC LINK (Send & Login)
    // =================================================================================
    public void sendLinkLogin(EmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isEnabled()) throw new BadRequestException("User is not active");
        if (!user.isAccountNonLocked()) throw new BadRequestException("Account is locked");

        magicLoginTokenRepository.deleteAllByUser(user);

        String token = UUID.randomUUID().toString();
        MagicLoginToken magicLoginToken = MagicLoginToken.builder()
                .token(token)
                .expiredAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .user(user)
                .build();
        magicLoginTokenRepository.save(magicLoginToken);

        String link = fullUrl + pathToLink + "?token=" + token;
        emailService.send(user.getEmail(), "Вхід без паролю", buildEmail(user.getFirstName(), link));
    }

    @Transactional
    public AuthResponse linkLogin(TokenRequest request, String sessionId, String userAgent) {
        MagicLoginToken token = magicLoginTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));

        if (!token.isValid()) {
            throw new BadRequestException("Magic link expired or used");
        }

        User user = token.getUser();
        token.setUsedAt(Instant.now());
        magicLoginTokenRepository.save(token);

//        updateLastSeenAsync(user.getId());

        if (sessionId != null) {
//            synchronizeService.syncCarts(sessionId, user);
        }
        return generateTokens(user, userAgent);
    }

    // =================================================================================
    // 7. RESET PASSWORD
    // =================================================================================
    public void sendResetPassword(EmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        resetPasswordTokenRepository.deleteAllByUser(user);

        String token = UUID.randomUUID().toString();
        ResetPasswordToken resetToken = ResetPasswordToken.builder()
                .token(token)
                .user(user)
                .expiredAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .build();
        resetPasswordTokenRepository.save(resetToken);

        String link = fullUrl + pathToReset + "?token=" + token;
        emailService.send(user.getEmail(), "Відновлення пароля", buildEmail(user.getFirstName(), link));
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        ResetPasswordToken token = resetPasswordTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid reset token"));

        if (!token.isValid()) {
            throw new BadRequestException("Reset link expired or used");
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.resetFailedAttempts();
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        resetPasswordTokenRepository.save(token);

        return "Password successfully updated";
    }

    // =================================================================================
    // HELPERS & ASYNC
    // =================================================================================

    private AuthResponse generateTokens(User user, String userAgentString) {
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Client c = uaParser.parse(userAgentString);
        String info = c.userAgent.family + " " + c.userAgent.major + " on " + c.os.family;

        RefreshToken tokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusMillis(refreshExpiration))
                .deviceInfo(info)
                .build();
        refreshTokenRepository.save(tokenEntity);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void createAndSendConfirmationToken(User user) {
        String token = UUID.randomUUID().toString();
        ConfirmationEmailToken entity = ConfirmationEmailToken.builder()
                .token(token)
                .expiredAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .user(user)
                .build();
        confirmationEmailTokenRepository.save(entity);

        String link = fullUrl + pathToConfirm + "?token=" + token;
        emailService.send(user.getEmail(), "Підтвердіть реєстрацію", buildEmail(user.getFirstName(), link));
    }

    private void handleFailedLogin(User user) {
        user.incrementFailedAttempts();
        if (user.getFailedLoginAttempts() >= 3) { // 3 спроби
            user.lockAccount(15); // Блок на 15 хв
        }
        userRepository.save(user);
    }

//    // 🔥 ВАЖЛИВО: Цей метод має бути в окремому біні або викликатись через self-injection,
//    // але для простоти можна залишити тут, якщо включено @EnableAsync.
//    // Найкраще - винести в UserActivityService.
//    @Async
//    @Transactional
//    public void updateLastSeenAsync(Long userId) {
//        // Використовуємо @Modifying @Query в репозиторії, щоб не тягнути весь об'єкт
//        userRepository.updateLastSeen(userId, Instant.now());
//    }

    private String buildEmail(String name, String link) {
        //Заглушка
        return link;
    }
}