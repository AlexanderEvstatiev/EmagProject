package finalproject.emag.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewDto {

    private long userId;
    private long productId;
    private String title;
    private String comment;
    private int grade;

}
