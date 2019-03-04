package finalproject.emag.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class PromotionProductDto {

    private long productId;
    private LocalDate startDate;
    private LocalDate endDate;
    private double oldPrice;
    private double newPrice;

}
