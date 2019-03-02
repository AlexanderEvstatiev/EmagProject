package finalproject.emag.model.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Review {

    @JsonIgnore
    private long userId;
    @JsonIgnore
    private long productId;
    private String title;
    private String comment;
    private int grade;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return userId == review.userId &&
                productId == review.productId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, productId);
    }
}
