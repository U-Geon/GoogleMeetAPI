package test.googlemeetapi.global.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import test.googlemeetapi.domain.Member;
import test.googlemeetapi.domain.member.service.LoadUserService;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final LoadUserService loadUserService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String accessToken = resolveToken(request);

        try {
            if(jwtProvider.validate(accessToken)) {
                String email = jwtProvider.getEmailFromAccessToken(accessToken);
                Authentication authentication = getAuthentication(email);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (TokenExpiredException e) {
            log.error("Access token expired.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        } catch (JWTVerificationException e) {
            log.error("JWTVerificationException: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION);
        String tokenPrefix = "Bearer ";
        if (token != null && token.startsWith(tokenPrefix)) {
            return token.substring(tokenPrefix.length());
        }
        return null;
    }

    // 인증 정보 가져오기
    private Authentication getAuthentication(String email) {
        Member userDetails = loadUserService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
