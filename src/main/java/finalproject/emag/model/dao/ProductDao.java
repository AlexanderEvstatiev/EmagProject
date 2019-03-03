package finalproject.emag.model.dao;

import finalproject.emag.model.dto.CartViewProductDto;
import finalproject.emag.model.dto.GlobalViewProductDto;
import finalproject.emag.model.pojo.Product;
import finalproject.emag.model.pojo.Review;
import finalproject.emag.model.pojo.Stat;
import finalproject.emag.model.pojo.User;
import finalproject.emag.util.exception.BaseException;
import finalproject.emag.util.exception.ProductNotFoundException;
import finalproject.emag.util.exception.ProductOutOfStockException;
import finalproject.emag.util.exception.WrongSearchWordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public Product getProductForCart(long id) throws Exception{
        checkIfProductExists(id);
        checkProductQuantity(id);
        try(Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT product_name, price, quantity FROM products WHERE id=?");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            Product p = new Product();
            while (rs.next()) {
                p.setId(id);
                p.setName(rs.getString(1));
                p.setPrice(rs.getDouble(2));
                p.setQuantity(rs.getInt(3));
            }
            return p;
        }
    }

    private void checkProductQuantity(long id) throws Exception {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT quantity, product_name FROM products WHERE id = ?");
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int quantity = rs.getInt(1);
            String name = rs.getString(2);
            if (quantity > 0) {
                return;
            }
            throw new ProductOutOfStockException("The product " + name + " is out of stock right now.");
        }
    }

    public ArrayList<CartViewProductDto> viewCart(HttpSession session) {
        HashMap<Product, Integer> products = (HashMap<Product, Integer>) session.getAttribute("cart");
        ArrayList<CartViewProductDto> cart = new ArrayList<>();
        for (Map.Entry<Product, Integer> e : products.entrySet()) {
            CartViewProductDto p = new CartViewProductDto();
            p.setId(e.getKey().getId());
            p.setName(e.getKey().getName());
            p.setQuantity(e.getValue());
            p.setPrice(e.getKey().getPrice() * e.getValue());
            cart.add(p);
        }
        return cart;
    }

    public void makeOrder(HttpSession session) throws Exception{
        HashMap<Product, Integer> products = (HashMap<Product, Integer>) session.getAttribute("cart");
        for (Product p : products.keySet()) {
            checkProductQuantity(p.getId());
        }
        double price = 0;
        for (Map.Entry<Product, Integer> e : products.entrySet() ){
            price += (e.getKey().getPrice() * e.getValue());
        }
        User u = (User) session.getAttribute("user");
        Connection c = jdbcTemplate.getDataSource().getConnection();
        try {
            c.setAutoCommit(false);
            long productId = insertOrder(u, c, price);
            insertOrderProducts(c, products, productId);
            updateQuantity(c, products);
            session.setAttribute("cart", null);
            c.commit();
        }
        catch (SQLException e) {
            c.rollback();
            throw new SQLException();
        }
        finally {
            c.setAutoCommit(true);
            c.close();
        }
    }

    private long insertOrder(User u, Connection c, double price) throws SQLException{
        PreparedStatement ps = c.prepareStatement("INSERT INTO orders (user_id, total_price, order_date) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setLong(1, u.getId());
        ps.setDouble(2, price);
        java.sql.Date date = Date.valueOf(LocalDate.now().toString());
        ps.setDate(3, date);
        ps.execute();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        return rs.getLong(1);
    }

    private void insertOrderProducts(Connection c, HashMap<Product, Integer> products, long orderId) throws SQLException {
        for (Map.Entry<Product, Integer> e : products.entrySet()) {
            PreparedStatement ps = c.prepareStatement("INSERT INTO ordered_products (order_id, product_id, quantity) VALUES (?, ?, ?)");
            ps.setLong(1, orderId);
            ps.setLong(2, e.getKey().getId());
            ps.setInt(3, e.getValue());
            ps.execute();
            ps.close();
        }
    }

    private void updateQuantity(Connection c, HashMap<Product, Integer> products) throws SQLException {
        for (Map.Entry<Product, Integer> e : products.entrySet()) {
            PreparedStatement ps = c.prepareStatement("UPDATE products SET quantity = quantity - ? WHERE id = ? ");
            ps.setInt(1, e.getValue());
            ps.setLong(2, e.getKey().getId());
            ps.execute();
            ps.close();
        }
    }
}
