package finalproject.emag.model.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Stat {

    @JsonIgnore
    private long id;
    @JsonIgnore
    private long subcategoryId;
    private String name;
    private String unit;
    private String value;

    public Stat(long id, long subcategoryId, String name, String unit, String value) {
        this.id = id;
        this.subcategoryId = subcategoryId;
        this.name = name;
        this.unit = unit;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stat stat = (Stat) o;
        return id == stat.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
