package finalproject.emag.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.emag.model.dao.ProductDao;
import finalproject.emag.model.dto.CartViewProductDto;
import finalproject.emag.model.dto.GlobalViewProductDto;
import finalproject.emag.model.pojo.Product;
import finalproject.emag.util.exception.EmptyCartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping(produces = "application/json")
public class ProductController extends BaseController {

    private static final int MIN_NUMBER_OF_PRODUCTS = 0;
    private static final int MAX_NUMBER_OF_PRODUCTS = 9999;
    private static final String MIN_PRICE = "0";
    private static final String MAX_PRICE = "99999";
    public static final String CART = "cart";

    @Autowired
    private ProductDao dao;
    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = ("/products"))
    public ArrayList<GlobalViewProductDto> getAllProducts() throws SQLException {
        return dao.getAllProducts();
    }

    @GetMapping(value = ("/products/filter"))
    public ArrayList<GlobalViewProductDto> getAllProductsFiltered(@RequestParam(value = "order", required = false, defaultValue = "ASC") String order,
                                                                  @RequestParam(value = "from", required = false, defaultValue = MIN_PRICE) Double min,
                                                                  @RequestParam(value = "to", required = false, defaultValue = MAX_PRICE) Double max
    ) throws Exception {
        String sql = generateSql(order, min, max, "");
        return dao.getAllProductsFiltered(sql);
    }

    @GetMapping(value = ("/products/subcategory/{id}"))
    public ArrayList<GlobalViewProductDto> getAllProductsBySubcategory(@PathVariable("id") long id) throws Exception {
        return dao.getAllProductsBySubcategory(id);
    }

    @GetMapping(value = ("/products/subcategory/{id}/filter"))
    public ArrayList<GlobalViewProductDto> getAllProductsBySubcategoryFiltered(@PathVariable(value = "id") long id,
                                                                               @RequestParam(value = "order", required = false, defaultValue = "ASC") String order,
                                                                               @RequestParam(value = "from", required = false, defaultValue = MIN_PRICE) Double min,
                                                                               @RequestParam(value = "to", required = false, defaultValue = MAX_PRICE) Double max) throws Exception {
        double minDb = dao.getMinPriceOfProductForSubcatecory(id);
        double maxDb = dao.getMaxPriceOfProductForSubcategory(id);
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
        return dao.getAllProductsBySubcategoryFiltered(id, sql);
    }

    private String generateSql(String order, Double min, Double max, String sub) throws Exception {
        String sql = "SELECT id, product_name, price, quantity FROM products;";
        double minDb = dao.getMinPriceOfProduct();
        double maxDb = dao.getMaxPriceOfProduct();
        if (min < minDb || max < minDb || min > maxDb || max < min) {
            return sql;
        }
        if (order.equals("DESC")) {
            return "SELECT id, product_name, price, quantity FROM products WHERE price BETWEEN " + min + " AND " + max + sub + " ORDER BY price DESC";
        }
        if (order.equals("ASC")) {
            return "SELECT id, product_name, price, quantity FROM products WHERE price BETWEEN " + min + " AND " + max + sub + " ORDER BY price ASC";
        }
        return sql;
    }

    @GetMapping(value = ("/products/{id}"))
    public Product getProductById(@PathVariable("id") long id) throws Exception {
        return dao.getProductById(id);
    }

    @GetMapping(value = ("/products/search/{name}"))
    public ArrayList<GlobalViewProductDto> searchProducts(@PathVariable("name") String name) throws Exception {
        return dao.searchProducts(name);
    }

    @PostMapping(value = ("/products/{id}/add"))
    public String addToCart(@PathVariable("id") long id, HttpServletRequest request) throws Exception {
        validateLogin(request.getSession());
        HashMap<Product, Integer> cart = null;
        Product p = dao.getProductForCart(id);
        if (request.getSession().getAttribute("cart") != null ) {
            cart = (HashMap<Product, Integer>) request.getSession().getAttribute("cart");
            if(cart.containsKey(p)) {
                int quantity = cart.get(p);
                cart.put(p, quantity+1);
            }
            else {
                cart.put(p, 1);
            }
        }
        else {
            request.getSession().setAttribute(CART, new HashMap<Product, Integer>());
            cart = (HashMap<Product, Integer>) request.getSession().getAttribute("cart");
            cart.put(p, 1);
        }
        return p.getName() + " was added to your cart.";
    }

    @GetMapping(value = ("/view/cart"))
    public ArrayList<CartViewProductDto> viewCart(HttpServletRequest request) throws Exception{
        validateLogin(request.getSession());
        if (request.getSession().getAttribute(CART) != null) {
            return dao.viewCart(request.getSession());
        }
        else {
            throw new EmptyCartException();
        }
    }

    @PostMapping(value = ("/view/cart/order"))
    public String makeOrder(HttpServletRequest request) throws Exception {
        validateLogin(request.getSession());
        if (request.getSession().getAttribute(CART) != null) {
            dao.makeOrder(request.getSession());
            return "Your order was successful.";
        }
        else {
            throw new EmptyCartException();
        }
    }

}
