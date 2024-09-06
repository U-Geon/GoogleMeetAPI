package test.googlemeetapi.googlemeet;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import test.googlemeetapi.googlemeet.GoogleCalendarService;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

@Service
public class DistributeService {

    // 애플리케이션에서 사용하는 Google 클라이언트 ID를 주입받음
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    // 애플리케이션에서 사용하는 Google 클라이언트 시크릿을 주입받음
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    // 애플리케이션 이름 설정
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";

    // JSON 처리에 사용할 JsonFactory 설정 (Gson 사용)
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // OAuth 클라이언트 파일 경로
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Google OAuth 인증 URL을 생성하는 메서드
     * 사용자에게 인증 URL을 제공하여 Google 계정으로 로그인하고 권한을 부여받음
     * @return Google OAuth 인증 URL
     * @throws Exception 예외 처리
     */
    public String getAuthorizationUrl() throws Exception {
        // credentials.json 파일을 로드하여 Google 클라이언트 비밀 정보를 얻음
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(Objects.requireNonNull(in)));

        // GoogleAuthorizationCodeFlow를 사용하여 OAuth 인증 흐름을 설정
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, Collections.singletonList("https://www.googleapis.com/auth/calendar"))
                .setAccessType("offline") // 오프라인 접근 권한 설정
                .build();

        // 사용자가 Google 계정으로 인증할 수 있는 URL 생성 및 반환
        return flow.newAuthorizationUrl().setRedirectUri("http://localhost:8888/Callback").build();
    }

    /**
     * OAuth 2.0 인증 코드를 사용하여 Google Meet 이벤트를 생성하는 메서드
     * @param code OAuth 2.0 인증 코드
     * @param summary 이벤트 제목
     * @param description 이벤트 설명
     * @param location 이벤트 위치
     * @param startDateTime 이벤트 시작 시간
     * @param endDateTime 이벤트 종료 시간
     * @param attendeesEmails 참석자 이메일 목록
     * @return 생성된 Google Meet URL
     * @throws Exception 예외 처리
     */
    public String createEventWithCode(String code, String summary, String description, String location, String startDateTime, String endDateTime, String[] attendeesEmails) throws Exception {
        // 인증 코드를 사용하여 액세스 토큰을 얻음
        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                "https://oauth2.googleapis.com/token",
                clientId,  // Google API Console에서 얻은 클라이언트 ID
                clientSecret,  // Google API Console에서 얻은 클라이언트 시크릿
                code,
                "http://localhost:8888/Callback")  // 리디렉션 URI
                .execute();

        // BearerToken을 사용하여 Credential 객체를 생성하고 액세스 토큰을 설정
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(tokenResponse.getAccessToken());

        // Google Calendar API 서비스 객체를 생성
        Calendar service = new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        // 이벤트 객체 생성 및 기본 정보 설정
        Event event = new Event()
                .setSummary(summary) // 이벤트 제목 설정
                .setLocation(location) // 이벤트 위치 설정
                .setDescription(description); // 이벤트 설명 설정

        // 이벤트 시작 시간 설정 (KST 타임존 사용)
        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(startDateTime)) // 시작 날짜 및 시간 설정
                .setTimeZone("Asia/Seoul");  // 한국 표준시(KST) 설정
        event.setStart(start);

        // 이벤트 종료 시간 설정 (KST 타임존 사용)
        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(endDateTime)) // 종료 날짜 및 시간 설정
                .setTimeZone("Asia/Seoul"); // 한국 표준시(KST) 설정
        event.setEnd(end);

        // 참석자 목록 생성 및 설정
        EventAttendee[] attendees = new EventAttendee[attendeesEmails.length];
        for (int i = 0; i < attendeesEmails.length; i++) {
            attendees[i] = new EventAttendee().setEmail(attendeesEmails[i]); // 각 참석자의 이메일 설정
        }
        event.setAttendees(Arrays.asList(attendees));

        // Google Meet 회의 데이터를 설정
        ConferenceData conferenceData = new ConferenceData();
        ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey();
        conferenceSolutionKey.setType("hangoutsMeet"); // Google Meet 회의 타입 설정
        CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest();
        createConferenceRequest.setRequestId("some-random-string"); // 유니크한 요청 ID 설정
        createConferenceRequest.setConferenceSolutionKey(conferenceSolutionKey); // 회의 솔루션 키 설정
        conferenceData.setCreateRequest(createConferenceRequest); // 회의 데이터에 요청 설정
        event.setConferenceData(conferenceData); // 이벤트에 회의 데이터 설정

        // Google Calendar API를 통해 이벤트를 삽입하고 생성된 이벤트를 반환
        Event createdEvent = service.events().insert("primary", event)
                .setConferenceDataVersion(1) // 회의 데이터 버전 설정
                .execute(); // 이벤트 생성 요청 실행

        // Google Meet URL을 추출
        ConferenceData createdConferenceData = createdEvent.getConferenceData();
        String googleMeetLink = null;
        if (createdConferenceData != null && createdConferenceData.getEntryPoints() != null) {
            for (EntryPoint entryPoint : createdConferenceData.getEntryPoints()) {
                if ("video".equals(entryPoint.getEntryPointType())) {  // Google Meet 링크인지 확인
                    googleMeetLink = entryPoint.getUri();  // Google Meet URL 추출
                    break;
                }
            }
        }

        // Google Meet URL 반환 (없을 경우 기본 메시지 반환)
        return googleMeetLink != null ? googleMeetLink : "No Google Meet link available";
    }
}
