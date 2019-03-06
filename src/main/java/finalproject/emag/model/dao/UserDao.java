package finalproject.emag.model.dao;

import finalproject.emag.model.dto.EditEmailDto;
import finalproject.emag.model.dto.EditPasswordDto;
import finalproject.emag.model.dto.EditPersonalInfoDto;
import finalproject.emag.model.pojo.User;
import finalproject.emag.util.PasswordEncoder;
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
            PreparedStatement ps = connection.prepareStatement("SELECT id,email,password,full_name,username," +
                    "phone_number,birth_date,subscribed,admin,image_url FROM users WHERE email LIKE ?");
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
            PreparedStatement ps = connection.prepareStatement("INSERT INTO users " +
                    "(email,password,full_name,username,phone_number,birth_date,subscribed) " +
                    "VALUES (?,?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, PasswordEncoder.hashPassword(user.getPassword()));
            ps.setString(3, user.getName());
            ps.setString(4, user.getUsername());
            ps.setString(5, user.getPhoneNumber());
            ps.setDate(6, user.getBirthDate() == null ? null :
                    java.sql.Date.valueOf(user.getBirthDate()));
            ps.setBoolean(7, user.isSubscribed());
            ps.executeUpdate();
            ResultSet result = ps.getGeneratedKeys();
            result.next();
            user.setId(result.getLong(1));
        }
    }
    public boolean checkIfEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email LIKE ?";
        Integer emailCheck = template.queryForObject(sql, Integer.class, email);
        return emailCheck == null || emailCheck <= 0;
    }

    public boolean checkIfUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username LIKE ?";
        Integer usernameCheck = template.queryForObject(sql, Integer.class, username);
        return usernameCheck == null || usernameCheck <= 0;
    }
    public void editPersonalInfoUser(EditPersonalInfoDto user, long userId) throws SQLException {
        try(Connection connection = this.template.getDataSource().getConnection()) {
            String sql = "UPDATE users SET full_name = ?,username = ?,phone_number = ?,birth_date = ? WHERE id LIKE ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPhoneNumber());
            ps.setDate(4, user.getBirthDate() == null ? null :
                    java.sql.Date.valueOf(user.getBirthDate()));
            ps.setLong(5, userId);
            ps.executeUpdate();
        }
    }

    public void editEmail(EditEmailDto user, long userId) throws SQLException {
        try(Connection connection = this.template.getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement("UPDATE users SET email = ? WHERE id = ?");
            ps.setString(1, user.getEmail());
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    public void editPassword(EditPasswordDto user, long userId) throws SQLException {
        try(Connection connection = this.template.getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement("UPDATE users SET password = ? WHERE id = ?");
            ps.setString(1, PasswordEncoder.hashPassword(user.getPassword()));
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    private boolean isSubscribed(User user) throws SQLException {
        try(Connection connection = this.template.getDataSource().getConnection()) {
            PreparedStatement ps = connection.prepareStatement("SELECT subscribed FROM users WHERE email LIKE ?");
            ps.setString(1, user.getEmail());
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getBoolean(1);
        }
    }

    public String subscribe(User user) throws SQLException {
        try (Connection connection = this.template.getDataSource().getConnection()){
            String sql = "UPDATE users SET subscribed = true WHERE email LIKE ?";
            PreparedStatement subscribe = connection.prepareStatement(sql);
            boolean subed = isSubscribed(user);
            if (subed) {
                return "You are already subscribed.";
            } else {
                subscribe.setString(1, user.getEmail());
                subscribe.executeUpdate();
                return "You are now subscribed.";
            }
        }
    }

    public String unsubscribe (User user) throws SQLException {
        try (Connection connection = this.template.getDataSource().getConnection()){
            boolean subed = isSubscribed(user);
            String sql = "UPDATE users SET subscribed = false WHERE email LIKE ?";
            PreparedStatement unsubscribe = connection.prepareStatement(sql);
            if (!subed) {
                return "You are not subscribed.";
            } else {
                unsubscribe.setString(1, user.getEmail());
                unsubscribe.executeUpdate();
                return "You are now unsubscribed.";
            }
        }
    }
    public void uploadImage(User user,String imageUrl) throws SQLException {
        try(Connection connection = this.template.getDataSource().getConnection()){
            PreparedStatement ps = connection.prepareStatement("UPDATE users SET image_url = ? WHERE id = ?");
            ps.setString(1,imageUrl);
            ps.setLong(2,user.getId());
            ps.executeUpdate();
        }
    }

    public String getImageUrl(long userId) throws SQLException {
        try(Connection connection = this.template.getDataSource().getConnection()){
            PreparedStatement ps = connection.prepareStatement("SELECT image_url FROM users WHERE id = ?");
            ps.setLong(1,userId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return rs.getString(1);
            }
            else{
                return null;
            }
        }
    }
}