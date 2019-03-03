package finalproject.emag.model.dao;

import finalproject.emag.model.dto.GlobalViewProductDto;
import finalproject.emag.model.pojo.Product;
import finalproject.emag.model.pojo.Review;
import finalproject.emag.model.pojo.Stat;
import finalproject.emag.util.exception.BaseException;
import finalproject.emag.util.exception.ProductNotFoundException;
import finalproject.emag.util.exception.WrongSearchWordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Component
public class ProductDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ProductDao() {
    }

    private ArrayList<GlobalViewProductDto> products(ResultSet rs) throws SQLException {
        ArrayList<GlobalViewProductDto> products = new ArrayList<>();
        while (rs.next()) {
            GlobalViewProductDto p = new GlobalViewProductDto();
            p.setId(rs.getLong(1));
            p.setName(rs.getString(2));
            p.setPrice(rs.getDouble(3));
            p.setQuantity(rs.getInt(4));
            p.setReviewsCount(getReviewsCountForProduct(rs.getLong(1)));
            p.setReviewsGrade(getReviewsAvgGradeForProduct(rs.getLong(1)));
            products.add(p);
        }
        return  products;
    }

    public ArrayList<GlobalViewProductDto> getAllProducts() throws SQLException {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT id, product_name, price, quantity FROM products;");
            ResultSet rs = ps.executeQuery();
            return products(rs);
        }
    }

    public ArrayList<GlobalViewProductDto> getAllProductsBySubcategory(long id) throws Exception {
        checkSubcategoryId(id);
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT id, product_name, price, quantity FROM products WHERE subcategory_id = ?;");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return products(rs);
        }
    }

    public ArrayList<GlobalViewProductDto> getAllProductsBySubcategoryFiltered(long id, String sql) throws Exception {
        checkSubcategoryId(id);
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            return products(rs);
        }
    }

    private void checkSubcategoryId(long id) throws Exception {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM products WHERE subcategory_id = ?;");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) <= 0) {
                throw new BaseException("No such subcategory!");
            }
        }
    }

    public ArrayList<GlobalViewProductDto> getAllProductsFiltered(String sql) throws SQLException {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            return products(rs);
        }
    }

    public double getMaxPriceOfProduct() throws SQLException {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT MAX(price) FROM emag.products;");
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getDouble(1);
        }
    }

    public double getMaxPriceOfProductForSubcategory(long id) throws SQLException {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT MAX(price) FROM emag.products WHERE subcategory_id = ?;");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getDouble(1);
        }
    }

    public double getMinPriceOfProduct() throws SQLException {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT MIN(price) FROM emag.products;");
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getDouble(1);
        }
    }

    public double getMinPriceOfProductForSubcatecory(long id) throws SQLException {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT MIN(price) FROM emag.products WHERE subcategory_id = ?;");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getDouble(1);
        }
    }

    private int getReviewsCountForProduct(long id) throws SQLException{
        try(Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM reviews WHERE product_id = ?");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }
    private int getReviewsAvgGradeForProduct(long id) throws SQLException{
        try(Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT ROUND(AVG(grade)) FROM reviews WHERE product_id = ?");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public ArrayList<GlobalViewProductDto> searchProducts(String name) throws Exception {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT id, product_name, price, quantity FROM products WHERE product_name LIKE '%" + name + "%';");
            ResultSet rs = ps.executeQuery();
            ArrayList<GlobalViewProductDto> products = products(rs);
            if (products.size() == 0) {
                throw new WrongSearchWordException();
            }
            return products;
        }
    }

    public Product getProductById(long id) throws Exception {
        checkIfProductExists(id);
        try(Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT p.id, p.subcategory_id, p.product_name, p.price, p.quantity, p.image_url, s.stat_name, w.value, s.unit, s.id FROM emag.products AS p \n" +
                    "JOIN products_with_stats AS w\n" +
                    "ON(p.id = w.product_id)\n" +
                    "JOIN stats AS s\n" +
                    "ON(w.stat_id = s.id)\n" +
                    "WHERE p.id = ?;");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            Product p = new Product();
            while (rs.next()) {
                p.setId(rs.getLong(1));
                p.setSubcategoryId(rs.getLong(2));
                p.setName(rs.getString(3));
                p.setPrice(rs.getDouble(4));
                p.setQuantity(rs.getInt(5));
                p.setImageUrl(rs.getString(6));
                Stat s = new Stat();
                s.setName(rs.getString(7));
                s.setValue(rs.getString(8));
                s.setUnit(rs.getString(9));
                s.setId(rs.getLong(10));
                s.setSubcategoryId(rs.getLong(2));
                p.addToStats(s);
                addReviewsToProduct(p, rs.getLong(1));
            }
            return p;
        }
    }

    private void addReviewsToProduct(Product p, long id) throws SQLException{
        try(Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT user_id, title, comment, grade FROM reviews WHERE product_id = ?");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Review review = new Review();
                review.setUserId(rs.getLong(1));
                review.setProductId(id);
                review.setTitle(rs.getString(2));
                review.setComment(rs.getString(3));
                review.setGrade(rs.getInt(4));
                p.addToReviews(review);
            }
        }
    }

    private void checkIfProductExists(long id) throws Exception {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM products WHERE id = ?");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int productExists = rs.getInt(1);
            if (productExists != 0) {
                return;
            }
            throw new ProductNotFoundException("This product does not exist!");
        }
    }
}
