package finalproject.emag.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemovePromotionDto {

    private long productId;
    private double price;

    public RemovePromotionDto(long productId) {
        this.productId = productId;
    }
}
