package finalproject.emag.model.dao;

import finalproject.emag.model.dto.*;
import finalproject.emag.model.pojo.Product;
import finalproject.emag.model.pojo.Review;
import finalproject.emag.model.pojo.Stat;
import finalproject.emag.model.pojo.User;
import finalproject.emag.util.MailUtil;
import finalproject.emag.util.exception.BaseException;
import finalproject.emag.util.exception.ProductNotFoundException;
import finalproject.emag.util.exception.ProductOutOfStockException;
import finalproject.emag.util.exception.WrongSearchWordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    private String generateSql(String order, Double min, Double max, String sub) throws Exception {
        String sql = "SELECT id, product_name, price, quantity FROM products;";
        if (order.equals("DESC")) {
            return "SELECT id, product_name, price, quantity FROM products" +
                    " WHERE price BETWEEN " + min + " AND " + max + sub + " ORDER BY price DESC";
        }
        if (order.equals("ASC")) {
            return "SELECT id, product_name, price, quantity FROM products" +
                    " WHERE price BETWEEN " + min + " AND " + max + sub + " ORDER BY price ASC";
        }
        return sql;
    }

    public ArrayList<GlobalViewProductDto> getAllProducts() throws SQLException {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT id, product_name, price, quantity FROM products;");
            ResultSet rs = ps.executeQuery();
            return products(rs);
        }
    }

    public ArrayList<GlobalViewProductDto> getAllProductsBySubcategory(long subcatId) throws Exception {
        checkSubcategoryId(subcatId);
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            String sql = "SELECT id, product_name, price, quantity FROM products WHERE subcategory_id = ?;";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, subcatId);
            ResultSet rs = ps.executeQuery();
            return products(rs);
        }
    }

    public ArrayList<GlobalViewProductDto> getAllProductsBySubcategoryFiltered(
            long id, String order, Double min, Double max) throws Exception {
        checkSubcategoryId(id);
        double minDb = getMinPriceOfProductForSubcategory(id);
        double maxDb = getMaxPriceOfProductForSubcategory(id);
        if (min < minDb) {
            min = minDb;
        }
        if (max < minDb) {
            max = minDb;
        }
        if (min > maxDb) {
            min = maxDb;
        }
        String sql = generateSql(order, min, max, " AND subcategory_id = " + id + " ");
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            return products(rs);
        }
    }

    private void checkSubcategoryId(long subcatId) throws Exception {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM products WHERE subcategory_id = ?;");
            ps.setLong(1, subcatId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) <= 0) {
                throw new BaseException("No such subcategory!");
            }
        }
    }

    public ArrayList<GlobalViewProductDto> getAllProductsFiltered(String order, Double min, Double max)
            throws Exception {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            double minDb = getMinPriceOfProduct();
            double maxDb = getMaxPriceOfProduct();
            String sql = null;
            if (min < 0 || max < minDb || min > maxDb || max < min) {
                sql = "SELECT id, product_name, price, quantity FROM products;";
            }
            else {
                sql = generateSql(order, min, max, "");
            }
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            return products(rs);
        }
    }

    private double getMaxPriceOfProduct() throws SQLException {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            PreparedStatement ps = c.prepareStatement("SELECT MAX(price) FROM emag.products;");
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getDouble(1);
        }
    }

    private double getMaxPriceOfProductForSubcategory(long subcatId) throws SQLException {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            String sql = "SELECT MAX(price) FROM emag.products WHERE subcategory_id = ?;";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, subcatId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getDouble(1);
        }
    }

    private double getMinPriceOfProduct() throws SQLException {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            String sql = "SELECT MIN(price) FROM emag.products;";
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getDouble(1);
        }
    }

    private double getMinPriceOfProductForSubcategory(long subcatId) throws SQLException {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            String sql = "SELECT MIN(price) FROM emag.products WHERE subcategory_id = ?;";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, subcatId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getDouble(1);
        }
    }

    private int getReviewsCountForProduct(long productId) throws SQLException{
        try(Connection c = jdbcTemplate.getDataSource().getConnection();) {
            String sql = "SELECT COUNT(*) FROM reviews WHERE product_id = ?";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, productId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    private int getReviewsAvgGradeForProduct(long productId) throws SQLException{
        try(Connection c = jdbcTemplate.getDataSource().getConnection();) {
            String sql = "SELECT ROUND(AVG(grade)) FROM reviews WHERE product_id = ?";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, productId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        }
    }

    public ArrayList<GlobalViewProductDto> searchProducts(String name) throws Exception {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            String sql = "SELECT id, product_name, price, quantity FROM products " +
                    "WHERE product_name LIKE '%" + name + "%';";
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            ArrayList<GlobalViewProductDto> products = products(rs);
            if (products.size() == 0) {
                throw new WrongSearchWordException();
            }
            return products;
        }
    }

    public Product getProductById(long productId) throws Exception {
        checkIfProductExists(productId);
        try(Connection c = jdbcTemplate.getDataSource().getConnection();) {
            String sql = "SELECT p.id, p.subcategory_id, p.product_name, p.price, p.quantity, p.image_url, " +
                    "s.stat_name, w.value, s.unit, s.id FROM emag.products AS p \n" +
                    "JOIN products_with_stats AS w\n" +
                    "ON(p.id = w.product_id)\n" +
                    "JOIN stats AS s\n" +
                    "ON(w.stat_id = s.id)\n" +
                    "WHERE p.id = ?;";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, productId);
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
            String sql = "SELECT user_id, title, comment, grade FROM reviews WHERE product_id = ?";
            PreparedStatement ps = c.prepareStatement(sql);
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

    private void checkIfProductExists(long productId) throws Exception {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            String sql = "SELECT COUNT(*) FROM products WHERE id = ?";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, productId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int productExists = rs.getInt(1);
            if (productExists != 0) {
                return;
            }
            throw new ProductNotFoundException("This product does not exist!");
        }
    }

    public CartProductDto getProductForCart(long productId) throws Exception{
        checkIfProductExists(productId);
        checkProductQuantity(productId, 1);
        try(Connection c = jdbcTemplate.getDataSource().getConnection();) {
            String sql = "SELECT product_name, price, quantity FROM products WHERE id=?";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, productId);
            ResultSet rs = ps.executeQuery();
            CartProductDto p = new CartProductDto();
            while (rs.next()) {
                p.setId(productId);
                p.setName(rs.getString(1));
                p.setPrice(rs.getDouble(2));
            }
            return p;
        }
    }

    private void checkProductQuantity(long id, int products) throws Exception {
        try (Connection c = jdbcTemplate.getDataSource().getConnection();) {
            String sql = "SELECT quantity, product_name FROM products WHERE id = ?";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            int quantity = rs.getInt(1);
            String name = rs.getString(2);
            if (quantity >= products) {
                return;
            }
            throw new ProductOutOfStockException("The product " + name + " is out of stock right now.");
        }
    }

    public ArrayList<CartViewProductDto> viewCart(HashMap<CartProductDto, Integer> userCart) {
        HashMap<CartProductDto, Integer> products = userCart;
        ArrayList<CartViewProductDto> cart = new ArrayList<>();
        for (Map.Entry<CartProductDto, Integer> e : products.entrySet()) {
            CartViewProductDto p = new CartViewProductDto();
            p.setId(e.getKey().getId());
            p.setName(e.getKey().getName());
            p.setQuantity(e.getValue());
            p.setPrice(e.getKey().getPrice() * e.getValue());
            cart.add(p);
        }
        return cart;
    }

    public void makeOrder(User user, HashMap<CartProductDto, Integer> userCart) throws Exception{
        HashMap<CartProductDto, Integer> products = userCart;
        for (Map.Entry<CartProductDto, Integer> e : products.entrySet()) {
            checkProductQuantity(e.getKey().getId(), e.getValue());
        }
        double price = 0;
        for (Map.Entry<CartProductDto, Integer> e : products.entrySet() ){
            price += (e.getKey().getPrice() * e.getValue());
        }
        Connection c = jdbcTemplate.getDataSource().getConnection();
        try {
            c.setAutoCommit(false);
            long productId = insertOrder(user, c, price);
            insertOrderProducts(c, products, productId);
            updateQuantity(c, products);
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
        String sql = "INSERT INTO orders (user_id, total_price, order_date) VALUES (?, ?, ?)";
        PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setLong(1, u.getId());
        ps.setDouble(2, price);
        java.sql.Date date = Date.valueOf(LocalDate.now().toString());
        ps.setDate(3, date);
        ps.execute();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        return rs.getLong(1);
    }

    private void insertOrderProducts(Connection c, HashMap<CartProductDto, Integer> products, long orderId)
            throws SQLException {
        for (Map.Entry<CartProductDto, Integer> e : products.entrySet()) {
            String sql = "INSERT INTO ordered_products (order_id, product_id, quantity) VALUES (?, ?, ?)";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, orderId);
            ps.setLong(2, e.getKey().getId());
            ps.setInt(3, e.getValue());
            ps.execute();
            ps.close();
        }
    }

    private void updateQuantity(Connection c, HashMap<CartProductDto, Integer> products) throws SQLException {
        for (Map.Entry<CartProductDto, Integer> e : products.entrySet()) {
            String sql = "UPDATE products SET quantity = quantity - ? WHERE id = ? ";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, e.getValue());
            ps.setLong(2, e.getKey().getId());
            ps.execute();
            ps.close();
        }
    }

    public void changeQuantity(long id, int quantity) throws Exception {
        checkIfProductExists(id);
        try(Connection c = jdbcTemplate.getDataSource().getConnection();) {
            String sql = "UPDATE products SET quantity= ? WHERE id= ?";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, quantity);
            ps.setLong(2, id);
            ps.execute();
        }
    }

    public void deleteProduct(long id) throws Exception {
        checkIfProductExists(id);
        try(Connection c = jdbcTemplate.getDataSource().getConnection();) {
            String sql = "DELETE FROM products WHERE id=?";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, id);
            ps.execute();
        }
    }

    public void insertProductInDB(AddProductDto product) throws SQLException {
        Connection c = jdbcTemplate.getDataSource().getConnection();
        try {
            c.setAutoCommit(false);
            long id = addProduct(c, product);
            addStats(c, product, id);
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

    private long addProduct(Connection c, AddProductDto product) throws SQLException {
        String sql = "INSERT INTO products (subcategory_id, product_name, price, quantity, image_url) " +
                "VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        ps.setLong(1, product.getSubcategoryId());
        ps.setString(2, product.getName());
        ps.setDouble(3, product.getPrice());
        ps.setInt(4, product.getQuantity());
        ps.setString(5, product.getImage());
        ps.execute();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        return rs.getLong(1);
    }

    private void addStats(Connection c, AddProductDto product, long id) throws SQLException {
        HashSet<Stat> stats = product.getStats();
        for (Stat stat : stats) {
            String sql = "INSERT INTO products_with_stats (product_id, stat_id, value) VALUES (?, ?, ?)";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, id);
            ps.setLong(2, stat.getId());
            ps.setString(3, stat.getValue());
            ps.execute();
            ps.close();
        }

    }

    public void addPromotion(PromotionProductDto product) throws SQLException, MessagingException {
        Connection connection = null;
        try{
            connection = this.jdbcTemplate.getDataSource().getConnection();
            connection.setAutoCommit(false);
            PreparedStatement putPromotion = connection.prepareStatement("INSERT INTO product_promotions" +
                    "(product_id,start_date,end_date,old_price,new_price) VALUES (?,?,?,?,?)");
            PreparedStatement updateProduct = connection.prepareStatement("UPDATE products SET price = ? " +
                    "WHERE id = ?");
            putPromotion.setLong(1,product.getProductId());
            putPromotion.setDate(2,product.getStartDate() == null ? null :
                    java.sql.Date.valueOf(product.getStartDate()));
            putPromotion.setDate(3,product.getEndDate() == null ? null :
                    java.sql.Date.valueOf(product.getEndDate()));
            putPromotion.setDouble(4,product.getOldPrice());
            putPromotion.setDouble(5,product.getNewPrice());
            putPromotion.executeUpdate();
            updateProduct.setDouble(1,product.getNewPrice());
            updateProduct.setLong(2,product.getProductId());
            updateProduct.executeUpdate();
            PreparedStatement ps = connection.prepareStatement("SELECT product_name FROM products WHERE id = ?");
            ps.setLong(1,product.getProductId());
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                String productName = rs.getString(1);
                notifyForPromotion("Promotion on "+productName,"We have a new special offer on " +
                        productName + " from " + product.getOldPrice()+" to "+product.getNewPrice());
            }
            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw new SQLException();
        }
        finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }
    private void notifyForPromotion(String title,String message) throws SQLException, MessagingException {
        ArrayList<NotifyUserDto> users = new ArrayList<>();
        try(Connection connection = this.jdbcTemplate.getDataSource().getConnection()) {
            String sql = "SELECT email,full_name FROM users WHERE subscribed = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setBoolean(1,true);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                users.add(new NotifyUserDto(rs.getString(1),rs.getString(2)));
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run(){
                for (NotifyUserDto user : users){
                    try {
                        MailUtil.sendMail("testingemag19@gmail.com",user.getEmail(),title,message);
                    } catch (MessagingException e) {
                        System.out.println("Ops there was a problem sending the email.");
                    }
                }
            }
        }).start();
    }

    public void removePromotion(RemovePromotionDto promo) throws SQLException {
        Connection connection = null;
        try {
            connection = this.jdbcTemplate.getDataSource().getConnection();
            connection.setAutoCommit(false);
            PreparedStatement ps = connection.prepareStatement("SELECT old_price FROM product_promotions " +
                    "WHERE product_id = ?");
            ps.setLong(1,promo.getProductId());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                promo.setPrice(rs.getDouble(1));
            }
            PreparedStatement removePromo = connection.prepareStatement("DELETE FROM product_promotions " +
                    "WHERE product_id = ?");
            removePromo.setLong(1,promo.getProductId());
            removePromo.executeUpdate();
            PreparedStatement putPrice = connection.prepareStatement("UPDATE products SET price = ? " +
                    "WHERE id = ? ");
            putPrice.setDouble(1,promo.getPrice());
            putPrice.setLong(2,promo.getProductId());
            putPrice.executeUpdate();
            connection.commit();

        }
        catch (SQLException e){
            connection.rollback();
            throw new SQLException();
        }
        finally {
            connection.setAutoCommit(true);
            connection.close();
        }
    }
}
