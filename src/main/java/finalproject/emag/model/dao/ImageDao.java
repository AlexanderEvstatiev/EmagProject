package finalproject.emag.model.dao;

import finalproject.emag.model.pojo.Product;
import finalproject.emag.model.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ImageDao {

    @Autowired
    private JdbcTemplate template;

    public void uploadProductImage(Product product,String imageUrl) throws SQLException{
        try(Connection connection = this.template.getDataSource().getConnection()){
            PreparedStatement ps = connection.prepareStatement("UPDATE products SET image_url = ? WHERE id = ?");
            ps.setString(1,imageUrl);
            ps.setLong(2,product.getId());
            ps.executeUpdate();
        }
    }

    public void uploadUserImage(User user, String imageUrl) throws SQLException {
        try(Connection connection = this.template.getDataSource().getConnection()){
            PreparedStatement ps = connection.prepareStatement("UPDATE users SET image_url = ? WHERE id = ?");
            ps.setString(1,imageUrl);
            ps.setLong(2,user.getId());
            ps.executeUpdate();
        }
    }

    public String getUserImageUrl(long userId) throws SQLException {
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
    public String getProductImageUrl(long productId) throws SQLException {
        try(Connection connection = this.template.getDataSource().getConnection()){
            PreparedStatement ps = connection.prepareStatement("SELECT image_url FROM products WHERE id = ?");
            ps.setLong(1,productId);
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
