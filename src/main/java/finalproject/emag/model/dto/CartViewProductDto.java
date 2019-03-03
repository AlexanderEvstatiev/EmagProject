package finalproject.emag.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartViewProductDto {

    private long id;
    private String name;
    private int quantity;
    private double price;

}
