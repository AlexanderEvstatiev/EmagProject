package finalproject.emag.model.pojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
public class Product {

    private long id;
    private long subcategoryId;
    private String name;
    private double price;
    private int quantity;
    private String imageUrl;
    private HashSet<Stat> stats = new HashSet<>();
    private HashSet<Review> reviews = new HashSet<>();

    public void addToStats(Stat stat) {
        stats.add(stat);
    }

    public void addToReviews(Review review) {
        reviews.add(review);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
