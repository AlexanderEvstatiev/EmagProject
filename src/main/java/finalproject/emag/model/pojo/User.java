package finalproject.emag.model.pojo;

import finalproject.emag.model.pojo.Order;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;

@NoArgsConstructor
@Getter
@Setter
public class User {

    private long id;
    private String email;
    private String password;
    private String name;
    private String username;
    private String phoneNumber;
    private LocalDate birthDate;
    private boolean subscribed;
    private boolean admin;
    private String imageUrl;
    private HashSet<Order> orders;

    public User(String email, String password, String name, String username, String phoneNumber, LocalDate birthDate, boolean subscribed,boolean admin,String imageUrl) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.subscribed = subscribed;
        this.admin = admin;
        this.imageUrl = imageUrl;
    }

    public User(String name, String username, String phoneNumber, LocalDate birthDate, boolean subscribed,String email) {
        this.name = name;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.birthDate = birthDate;
        this.subscribed = subscribed;
        this.email = email;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}