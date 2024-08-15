package test.googlemeetapi.global.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class JwtProvider {

    private final Algorithm algorithm;

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenValidTime; // 24시간

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenValidTime; // 2주

    public JwtProvider(@Value("${jwt.secret-key}") String secretKey) {
        this.algorithm = Algorithm.HMAC256(secretKey);
    }

    public String getEmailFromAccessToken(String token) {
        return JWT.decode(token).getClaim("email").asString();
    }

    public boolean validate(String token) {
        try {
            JWT.require(algorithm).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    // 액세스 토큰 생성
    public String createAccessToken(String email, String role) {
        return JWT.create()
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(accessTokenValidTime, ChronoUnit.HOURS))
                .withClaim("email", email)
                .withSubject(role)
                .sign(algorithm);
    }

    // 리프레시 토큰 생성
    public String createRefreshToken() {
        return JWT.create()
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(refreshTokenValidTime, ChronoUnit.HOURS))
                .sign(algorithm);
    }

    // JWT 만료 시간 확인
    public Long getTokenExpiration(String token) {
        return JWT.require(algorithm).build().verify(token).getExpiresAt().getTime();
    }

}
