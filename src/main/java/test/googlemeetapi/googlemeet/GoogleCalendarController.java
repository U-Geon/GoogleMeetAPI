package test.googlemeetapi.googlemeet;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import test.googlemeetapi.domain.Member;

@RestController
@RequiredArgsConstructor
public class GoogleCalendarController {

    private final GoogleCalendarService googleCalendarService;

    @PostMapping("/createMeeting")
    public String createMeeting(
            @RequestBody DTO dto) {
        try {
            String eventUrl = googleCalendarService.createEventAndGetMeetURL(dto.getSummary(), dto.getDescription(), dto.getLocation(), dto.getStartDateTime(), dto.getEndDateTime(), dto.getAttendeesEmails());
            return "Meeting created successfully: " + eventUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }

    @GetMapping("/")
    public ResponseEntity<String> test(@AuthenticationPrincipal Member member) {
        return ResponseEntity.ok(member.getEmail());
    }
}
