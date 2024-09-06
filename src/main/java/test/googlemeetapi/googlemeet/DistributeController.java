package test.googlemeetapi.googlemeet;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class DistributeController {

    private final DistributeService service;

    // 1. Google OAuth URL 반환 엔드포인트
    @GetMapping("/getGoogleAuthUrl")
    public String getGoogleAuthUrl() {
        try {
            String authorizationUrl = service.getAuthorizationUrl();
            return "Please open the following address in your browser:\n" + authorizationUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating Google OAuth URL";
        }
    }

    // 2. OAuth Callback을 처리하고 Google Meet URL을 반환하는 엔드포인트
    // 콜백 URL 인증 후 code 부분을 파싱하여 Parameter에 넣어야 함
    @GetMapping("/Callback")
    public String handleGoogleCallback(@RequestParam String code) {
        try {
            // 이 예제에서는 임의의 이벤트 정보를 사용합니다.
            String summary = "meeting test?";
            String description = "Discuss project updates";
            String location = "Hangout";
            String startDateTime = "2024-08-17T09:00:00+09:00";
            String endDateTime = "2024-08-17T10:00:00+09:00";
            String[] attendeesEmails = {"ryu7844@gmail.com", "hugemouth24@gmail.com"};

            String googleMeetLink = service.createEventWithCode(
                    code, summary, description, location, startDateTime, endDateTime, attendeesEmails);

            return "Google Meet URL: " + googleMeetLink;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error creating Google Meet event";
        }
    }
}
