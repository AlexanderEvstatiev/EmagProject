package finalproject.emag.model.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
public class Order {

    private long id;
    private long userId;
    private double price;
    private LocalDateTime date;
    private HashMap<Product, Integer> products;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
