package test.googlemeetapi.global.oauth2;

import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import test.googlemeetapi.domain.Member;
import test.googlemeetapi.global.jwt.JwtProvider;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("success handler 실행");

        if (authentication.getPrincipal() instanceof OAuth2User) {
            CustomOauth2UserDetails principal = (CustomOauth2UserDetails) authentication.getPrincipal();

            Member member = principal.getMember();
            log.info("member : {} {} ", member.getId(), member.getEmail()); // 수정된 부분

            String accessToken = jwtProvider.createAccessToken(member.getEmail(), member.getRole());

            if (member.getEmail().equals("ryu7844@gmail.com")) {
                String redirectionUri = UriComponentsBuilder.fromUriString("http://localhost:3000/hello")
                        .queryParam("email", member.getEmail())
                        .build()
                        .toUriString();
                response.sendRedirect(redirectionUri);

            } else {
                // refresh token을 저장해야 하는데 redis 사용 안하고 그냥
//                String refreshToken = jwtProvider.createRefreshToken(member.getEmail());
//                jwtService.save(new RefreshToken(refreshToken, member.getId()));

                String redirectionUri = UriComponentsBuilder.fromUriString("http://localhost:3000/hello")
                        .queryParam("token", accessToken)
                        .build()
                        .toUriString();
                response.sendRedirect(redirectionUri);
            }
        } else {
            // OAuth2User가 아닌 경우
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}