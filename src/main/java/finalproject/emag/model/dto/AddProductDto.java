package finalproject.emag.model.dto;

import finalproject.emag.model.pojo.Stat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;

@Getter
@Setter
@NoArgsConstructor
public class AddProductDto {

    private long subcategoryId;
    private String name;
    private double price;
    private int quantity;
    private String image;
    private HashSet<Stat> stats = new HashSet<>();

    public AddProductDto(long subcategoryId, String name, double price, int quantity, String image) {
        this.subcategoryId = subcategoryId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.image = image;
    }

    public void addStat(Stat stat) {
        stats.add(stat);
    }
}
