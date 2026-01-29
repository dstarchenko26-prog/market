package ua.nulp.backend.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import ua.nulp.backend.entity.enums.UserRole;
import ua.nulp.backend.entity.enums.UserStatus;
import ua.nulp.backend.entity.user.token.RefreshToken;
import ua.nulp.backend.entity.user.user.User;
import ua.nulp.backend.entity.user.user.UserSettings;
import ua.nulp.backend.repository.RefreshTokenRepository;
import ua.nulp.backend.repository.UserRepository;
import ua.nulp.backend.security.JwtService;
import ua.nulp.backend.service.SynchronizeService;
import ua_parser.Client;
import ua_parser.Parser;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SynchronizeService synchronizeService;
    private final Parser uaParser;

    @Value("${frontend.full-url}")
    private String fullUrl;
    @Value("${app.path.oauth}")
    private String pathToOauth;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = getOrCreateUser(email, oAuth2User);

        String sessionId = getCookieValue(request, "X-Session-Id");

        if (sessionId != null) {
            try {
                //synchronizeService.syncCarts(sessionId, user);
                clearCookie(response, "X-Session-Id");
            } catch (Exception e) {
            }
        }

        //userRepository.updateLastSeen(user.getId(), Instant.now());

        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        Tokens tokens = generateTokens(user, userAgent);

        String targetUrl = UriComponentsBuilder.fromUriString(fullUrl + pathToOauth)
                .queryParam("accessToken", tokens.accessToken)
                .queryParam("refreshToken", tokens.refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private User getOrCreateUser(String email, OAuth2User oAuth2User) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            String firstName = oAuth2User.getAttribute("given_name");
            String lastName = oAuth2User.getAttribute("family_name");

            if (firstName == null) firstName = "User";
            if (lastName == null) lastName = "";

            User newUser = User.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(UserRole.BUYER)
                    .status(UserStatus.ACTIVE)
                    .password(null)
                    .trustScore(1.0)
                    .lastSeenAt(Instant.now())
                    .build();

            UserSettings settings = UserSettings.builder()
                    .user(newUser)
                    .language("UK")
                    .theme("LIGHT")
                    .emailNotification(true)
                    .build();
            newUser.setUserSettings(settings);

            return userRepository.save(newUser);
        });
    }

    private Tokens generateTokens(User user, String userAgent) {
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        String deviceInfo = "Unknown Device";
        if (userAgent != null) {
            Client c = uaParser.parse(userAgent);
            deviceInfo = c.userAgent.family + " " + c.userAgent.major + " on " + c.os.family;
        }

        RefreshToken tokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .createdAt(Instant.now())
                .expiredAt(Instant.now().plusMillis(refreshExpiration))
                .deviceInfo(deviceInfo)
                .build();

        refreshTokenRepository.save(tokenEntity);

        return new Tokens(accessToken, refreshToken);
    }

    private String getCookieValue(HttpServletRequest req, String cookieName) {
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if (c.getName().equals(cookieName)) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    private void clearCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private record Tokens(String accessToken, String refreshToken) {}
}