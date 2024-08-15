package test.googlemeetapi.googlemeet;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

@Service
public class GoogleCalendarService {

    // 애플리케이션 이름 설정
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";

    // JSON 처리에 사용할 JsonFactory 설정 (Gson 사용)
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // 인증 토큰 저장 디렉터리
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    // OAuth 클라이언트 파일 경로
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    // Google Calendar API 서비스 객체 생성
    private Calendar getCalendarService() throws Exception {
        // NetHttpTransport를 통해 HTTP 통신을 위한 객체 생성
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // OAuth 2.0 인증을 위한 Credential 객체 생성
        Credential credential = authorize(HTTP_TRANSPORT);

        // Calendar 서비스 객체를 생성하고 애플리케이션 이름을 설정
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // OAuth 2.0 인증을 수행하는 메서드
    private Credential authorize(final NetHttpTransport HTTP_TRANSPORT) throws Exception {
        // 리소스에서 클라이언트 비밀 정보를 로드
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(Objects.requireNonNull(in)));

        // GoogleAuthorizationCodeFlow 객체를 생성하여 인증 흐름을 설정
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Collections.singletonList("https://www.googleapis.com/auth/calendar"))
                .setAccessType("offline") // 오프라인 접근 권한 설정
                .build();

        // 로컬 서버를 통해 인증 코드를 수신할 수 있는 수신기 설정
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        // AuthorizationCodeInstalledApp 객체를 생성하여 인증을 처리
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    // Google Calendar에 이벤트를 생성하는 메서드
    public String createEventAndGetMeetURL(String summary, String description, String location, String startDateTime, String endDateTime, String[] attendeesEmails) throws Exception {
        // 이벤트 객체 생성 및 기본 정보 설정
        Event event = new Event()
                .setSummary(summary) // 회의 제목
                .setLocation(location) // 회의 location
                .setDescription(description); // 회의 설명

        // 이벤트 시작 시간 설정 (KST 타임존 사용)
        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(startDateTime)) // 시작 날짜 및 시간
                .setTimeZone("Asia/Seoul");  // 한국 표준시(KST)
        event.setStart(start);

        // 이벤트 종료 시간 설정 (KST 타임존 사용)
        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(endDateTime)) // 종료 날짜 및 시간
                .setTimeZone("Asia/Seoul"); // 한국 표준시(KST)
        event.setEnd(end);

        // 참석자 목록 생성
        EventAttendee[] attendees = new EventAttendee[attendeesEmails.length];
        for (int i = 0; i < attendeesEmails.length; i++) {
            attendees[i] = new EventAttendee().setEmail(attendeesEmails[i]); // 각 참석자의 이메일 설정
        }
        event.setAttendees(Arrays.asList(attendees));

        // Google Meet을 위한 회의 데이터 설정
        ConferenceData conferenceData = new ConferenceData();
        ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey();
        conferenceSolutionKey.setType("hangoutsMeet"); // Google Meet 회의 타입 설정
        CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest();
        createConferenceRequest.setRequestId("some-random-string"); // 유니크한 요청 ID 설정
        createConferenceRequest.setConferenceSolutionKey(conferenceSolutionKey); // 회의 솔루션 키 설정
        conferenceData.setCreateRequest(createConferenceRequest); // 회의 데이터에 요청 설정
        event.setConferenceData(conferenceData); // 이벤트에 회의 데이터 설정

        // Google Calendar API를 통해 이벤트를 삽입
        Event createdEvent = getCalendarService().events().insert("primary", event)
                .setConferenceDataVersion(1) // 회의 데이터 버전 설정
                .execute(); // 이벤트 생성 요청 실행

        // Google Meet URL 추출
        ConferenceData createdConferenceData = createdEvent.getConferenceData();
        String googleMeetLink = null;
        if (createdConferenceData != null && createdConferenceData.getEntryPoints() != null) {
            for (EntryPoint entryPoint : createdConferenceData.getEntryPoints()) {
                if ("video".equals(entryPoint.getEntryPointType())) {
                    googleMeetLink = entryPoint.getUri();
                    break;
                }
            }
        }

        return googleMeetLink != null ? googleMeetLink : "No Google Meet link available";
    }
}
