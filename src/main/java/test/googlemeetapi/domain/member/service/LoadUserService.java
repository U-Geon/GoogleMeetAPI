package test.googlemeetapi.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import test.googlemeetapi.domain.Member;
import test.googlemeetapi.domain.member.repository.AuthRepository;

@Service
@RequiredArgsConstructor
public class LoadUserService implements UserDetailsService {

    private final AuthRepository authRepository;

    @Override
    public Member loadUserByUsername(String email) throws UsernameNotFoundException {
        return authRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("가입되어 있지 않은 유저입니다."));
    }
}
