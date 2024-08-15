package test.googlemeetapi.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import test.googlemeetapi.global.jwt.JwtAuthenticationFilter;
import test.googlemeetapi.global.oauth2.OAuth2SuccessHandler;
import test.googlemeetapi.global.oauth2.OAuth2UserService;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2UserService oAuth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() { // security를 적용하지 않을 리소스
        return web -> web.ignoring()
                .requestMatchers("/error", "/favicon.ico");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // stateless한 rest api를 개발할 것이므로 csrf 공격에 대한 옵션은 꺼둔다.
                .csrf(AbstractHttpConfigurer::disable) // csrf 비활성화 -> cookie를 사용하지 않으면 꺼도 된다. (cookie를 사용할 경우 httpOnly(XSS 방어), sameSite(CSRF 방어)로 방어해야 한다.)
                .formLogin(AbstractHttpConfigurer::disable) // security 기본 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // REST API이므로 basic auth 사용 x
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // cors 설정
                // 특정 URL에 대한 권한 설정.
                .authorizeHttpRequests(authz -> authz
                        // 추가적인 URL 권한 설정
                        .requestMatchers("/").permitAll()
                        .anyRequest().authenticated() // 그 외 요청은 인증 필요.
                )
                .oauth2Login(login -> login
                        .userInfoEndpoint(userInfoEndpointConfig -> // OAuth2 로그인 성공 이후 사용자 정보를 가져올 때 설정
                                userInfoEndpointConfig.userService(oAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler) // 로그인 성공 이후 핸들러 처리 로직
                )
                // Token 로그인 방식에서는 session 필요 없음.
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // addFilterBefore(after, before)
        return httpSecurity.build();
    }

    // CORS 설정.
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.addAllowedHeader("*");
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("Authorization"); // Access-Control-Expose-Headers 사용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
