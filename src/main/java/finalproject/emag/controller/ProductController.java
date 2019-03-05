package finalproject.emag.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.emag.model.dao.ProductDao;
import finalproject.emag.model.dto.*;
import finalproject.emag.model.pojo.Product;
import finalproject.emag.model.pojo.Stat;
import finalproject.emag.model.pojo.User;
import finalproject.emag.util.GetDate;
import finalproject.emag.util.exception.EmptyCartException;
import finalproject.emag.util.exception.InvalidQuantityException;
import finalproject.emag.util.exception.MissingValuableFieldsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping(produces = "application/json")
public class ProductController extends BaseController {

    private static final int MIN_NUMBER_OF_PRODUCTS = 0;
    private static final int MAX_NUMBER_OF_PRODUCTS = 9999;
    private static final String MIN_PRICE = "0";
    private static final String MAX_PRICE = "99999";
    private static final String CART = "cart";
    private static final String USER = "user";

    @Autowired
    private ProductDao dao;
    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = ("/products"))
    public ArrayList<GlobalViewProductDto> getAllProducts() throws SQLException {
        return dao.getAllProducts();
    }

    @GetMapping(value = ("/products/filter"))
    public ArrayList<GlobalViewProductDto> getAllProductsFiltered(
            @RequestParam(value = "order", required = false, defaultValue = "ASC") String order,
            @RequestParam(value = "from", required = false, defaultValue = MIN_PRICE) Double min,
            @RequestParam(value = "to", required = false, defaultValue = MAX_PRICE) Double max
    ) throws Exception {
        return dao.getAllProductsFiltered(order, min, max);
    }

    @GetMapping(value = ("/products/subcategory/{id}"))
    public ArrayList<GlobalViewProductDto> getAllProductsBySubcategory(@PathVariable("id") long id) throws Exception {
        return dao.getAllProductsBySubcategory(id);
    }

    @GetMapping(value = ("/products/subcategory/{id}/filter"))
    public ArrayList<GlobalViewProductDto> getAllProductsBySubcategoryFiltered(
            @PathVariable(value = "id") long id,
            @RequestParam(value = "order", required = false, defaultValue = "ASC") String order,
            @RequestParam(value = "from", required = false, defaultValue = MIN_PRICE) Double min,
            @RequestParam(value = "to", required = false, defaultValue = MAX_PRICE) Double max) throws Exception {
        return dao.getAllProductsBySubcategoryFiltered(id, order, min, max);
    }

    @GetMapping(value = ("/products/{id}"))
    public Product getProductById(@PathVariable("id") long id) throws Exception {
        return dao.getProductById(id);
    }

    @GetMapping(value = ("/products/search/{name}"))
    public ArrayList<GlobalViewProductDto> searchProducts(@PathVariable("name") String name) throws Exception {
        return dao.searchProducts(name);
    }

    @PostMapping(value = ("/products/add"))
    public String addProduct(@RequestBody String input, HttpServletRequest request) throws Exception {
        validateLoginAdmin(request.getSession());
        JsonNode jsonNode = objectMapper.readTree(input);
        int subCatId = (jsonNode.path("subcaregoryId").intValue());
        String name = (jsonNode.path("name").asText());
        double price = (jsonNode.path("price").doubleValue());
        int quantity = (jsonNode.path("quantity").intValue());
        String image = (jsonNode.path("image").asText());
        AddProductDto product = new AddProductDto(subCatId, name, price, quantity, image);
        JsonNode arrNode = objectMapper.readTree(input).get("stats");
        if (arrNode.isArray()) {
            for (JsonNode objNode : arrNode) {
                long id = (objNode.path("id").longValue());
                long subId = (objNode.path("subcategoryId").longValue());
                String statName = (objNode.path("name").asText());
                String unit = (checkIfNull(objNode, "unit"));
                String value = (objNode.path("value").asText());
                Stat stat = new Stat(id, subId, name, unit, value);
                product.addStat(stat);
            }
        }
        dao.insertProductInDB(product);
        return "You successfully added the product to the DB!";
    }

    private String checkIfNull(JsonNode node,String input){
        if(node.has(input)) {
            return node.path(input).asText();
        }
        return null;
    }

    @PutMapping(value = ("/products/{id}/quantity/{quantity}"))
    public String changeProductQuantity(
            @PathVariable("id") long id,
            @PathVariable("quantity") int quantity, HttpServletRequest request) throws Exception {
        validateLoginAdmin(request.getSession());
        if (quantity >= MIN_NUMBER_OF_PRODUCTS && quantity <= MAX_NUMBER_OF_PRODUCTS) {
            dao.changeQuantity(id, quantity);
        }
        else {
            throw new InvalidQuantityException();
        }
        return "Product with id - " + id + " now has quantity - " + quantity + ".";
    }

    @DeleteMapping(value = ("/products/{id}/delete"))
    public String deleteProduct(@PathVariable("id") long id, HttpServletRequest request) throws Exception {
        validateLoginAdmin(request.getSession());
        dao.deleteProduct(id);
        return "The product with id - " + id + " has been removed.";
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
            HashMap<Product, Integer> cart = (HashMap<Product, Integer>) request.getSession().getAttribute(CART);
            return dao.viewCart(cart);
        }
        else {
            throw new EmptyCartException();
        }
    }

    @PostMapping(value = ("/view/cart/order"))
    public String makeOrder(HttpServletRequest request) throws Exception {
        validateLogin(request.getSession());
        if (request.getSession().getAttribute(CART) != null) {
            User user = (User) request.getSession().getAttribute(USER);
            HashMap<Product, Integer> cart = (HashMap<Product, Integer>) request.getSession().getAttribute(CART);
            dao.makeOrder(user, cart);
            request.getSession().setAttribute(CART, null);
            return "Your order was successful.";
        }
        else {
            throw new EmptyCartException();
        }
    }

    @PostMapping(value = "/products/promotions/{id}")
    public String addPromotion(@PathVariable("id") long productId,
                               @RequestBody String input,HttpServletRequest request) throws Exception {
        validateLoginAdmin(request.getSession());
        JsonNode jsonNode = this.objectMapper.readTree(input);
        if(!jsonNode.has("start_date")|| !jsonNode.has("end_date")||
                !jsonNode.has("old_price")|| !jsonNode.has("new_price")){
            throw new MissingValuableFieldsException();
        }
        else{
            LocalDate startDate = GetDate.getDate(jsonNode.get("start_date").textValue());
            LocalDate endDate = GetDate.getDate(jsonNode.get("end_date").textValue());
            double oldPrice = jsonNode.get("old_price").asDouble();
            double newPrice = jsonNode.get("new_price").asDouble();
            PromotionProductDto product = new PromotionProductDto(productId,startDate,endDate,oldPrice,newPrice);
            dao.addPromotion(product);
            return "Promotion added";
        }

    }

    @DeleteMapping(value = "/products/promotions/{id}")
    public String removePromotion(@PathVariable("id") long productId, HttpServletRequest request) throws Exception {
        validateLoginAdmin(request.getSession());
        RemovePromotionDto product = new RemovePromotionDto(productId);
        dao.removePromotion(product);
        return "Promotion removed";
    }
}
