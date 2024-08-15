package test.googlemeetapi.global.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import test.googlemeetapi.domain.Member;
import test.googlemeetapi.domain.member.repository.AuthRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final AuthRepository authRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // OAuth2UserRequest에서 제공자 정보를 가져옵니다.
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 사용자 정보를 가져와서 추가 처리를 합니다.
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String username = (String) attributes.get("name"); // Google에서 제공하는 사용자 이름 속성 예시
        String email = (String) attributes.get("email"); // Google에서 제공하는 이메일 속성 예시

        // DB 로직
        Member member = getMember(email, username);

        // 사용자 정보를 기반으로 UserDetails 객체를 생성합니다.
        return new CustomOauth2UserDetails(member, attributes);
    }

    private Member getMember(String email, String username) {
        Member member = authRepository.findByEmail(email).orElse(Member.create(username, email));
        return authRepository.save(member);
    }
}
