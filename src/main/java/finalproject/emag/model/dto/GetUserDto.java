package finalproject.emag.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class GetUserDto {

    private long id;
    private String email;
    private String name;
    private String username;
    private String phoneNumber;
    private LocalDate birthDate;
    private boolean subscribed;
    private String imageUrl;

}
