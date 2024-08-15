package test.googlemeetapi.googlemeet;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DTO {
    private String summary;
    private String description;
    private String location;
    private String startDateTime;
    private String endDateTime;
    private String[] attendeesEmails;
}
