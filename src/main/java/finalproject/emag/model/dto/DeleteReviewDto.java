package finalproject.emag.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteReviewDto {

    private long userId;
    private long productId;
}
