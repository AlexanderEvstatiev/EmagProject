package finalproject.emag.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class GlobalViewProductDto {

    private long id;
    private String name;
    private double price;
    private int quantity;
    private int reviewsCount;
    private int reviewsGrade;

}
