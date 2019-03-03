package finalproject.emag.model.dao;

import finalproject.emag.model.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserDao {

    @Autowired
    private JdbcTemplate template;

    public User getUserByEmail(String email) throws SQLException {
        try (Connection connection = this.template.getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT id,email,password,full_name,username,phone_number,birth_date,subscribed,admin,image_url FROM users WHERE email LIKE ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User(rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getDate(7) == null ? null : rs.getDate(7).toLocalDate(),
                        rs.getBoolean(8),
                        rs.getBoolean(9),
                        rs.getString(10));
                user.setId(rs.getLong(1));
                return user;
            }
            return null;
        }
    }

    public void addUser(User user) throws SQLException {
        try (Connection connection = this.template.getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO users (email,password,full_name,username,phone_number,birth_date,subscribed) VALUES (?,?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getName());
            ps.setString(4, user.getUsername());
            ps.setString(5, user.getPhoneNumber());
            ps.setDate(6, user.getBirthDate() == null ? null : java.sql.Date.valueOf(user.getBirthDate()));
            ps.setBoolean(7, user.isSubscribed());
            ps.executeUpdate();
            ResultSet result = ps.getGeneratedKeys();
            result.next();
            user.setId(result.getLong(1));
        }
    }
    public boolean checkIfEmailExists(String email) {
        Integer emailCheck = template.queryForObject("SELECT COUNT(*) FROM users WHERE email LIKE ?", Integer.class, email);
        return emailCheck == null || emailCheck <= 0;
    }

    public boolean checkIfUsernameExists(String username) {
        Integer usernameCheck = template.queryForObject("SELECT COUNT(*) FROM users WHERE username LIKE ?", Integer.class, username);
        return usernameCheck == null || usernameCheck <= 0;
    }
}