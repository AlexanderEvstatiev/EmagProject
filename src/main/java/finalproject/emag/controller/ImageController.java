package finalproject.emag.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.emag.model.dao.ImageDao;
import finalproject.emag.model.dao.ProductDao;
import finalproject.emag.model.dao.UserDao;
import finalproject.emag.model.pojo.Product;
import finalproject.emag.model.pojo.User;
import finalproject.emag.model.pojo.messages.MessageSuccess;
import finalproject.emag.util.exception.ImageMissingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.time.LocalDateTime;
import java.util.Base64;

@RestController
@RequestMapping(value = "/images",produces = "application/json")
public class ImageController extends BaseController{

    private static final String IMAGE_PATH = "C:\\Users\\rache\\Desktop\\images\\";

    @Autowired
    private ImageDao dao;
    @Autowired
    private ProductDao productDao;
    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/users")
    public MessageSuccess uploadUserImage(@RequestBody String input, HttpSession session) throws Exception {
        validateLogin(session);
        User user = (User) session.getAttribute("user");
        JsonNode jsonNode = objectMapper.readTree(input);
        String name = uploadImage(jsonNode,user.getId());
        user.setImageUrl(name);
        this.dao.uploadUserImage(user,user.getImageUrl());
        return new MessageSuccess("Image upload successful", LocalDateTime.now());
    }
    @PostMapping("/products/{id}")
    public MessageSuccess uploadProductImage(@RequestBody String input, @PathVariable("id") long productId, HttpSession session) throws Exception {
        Product product = productDao.getProductById(productId);
        JsonNode jsonNode = objectMapper.readTree(input);
        String name = uploadImage(jsonNode,productId);
        product.setImageUrl(name);
        this.dao.uploadProductImage(product,product.getImageUrl());
        return new MessageSuccess("Image upload successful", LocalDateTime.now());
    }
    private String uploadImage(JsonNode jsonNode,long id) throws IOException {
        String base64 = jsonNode.get("image_url").textValue();
        byte[] bytes = Base64.getDecoder().decode(base64);
        String name = id+System.currentTimeMillis()+".png";
        File image = new File(IMAGE_PATH +name);
        FileOutputStream fos = new FileOutputStream(image);
        fos.write(bytes);
        fos.close();
        return name;
    }
    private byte[] getImage(String imageUrl) throws Exception {
        if(imageUrl==null){
            throw new ImageMissingException();
        }
        File image = new File(IMAGE_PATH+imageUrl);
        FileInputStream fis = new FileInputStream(image);
        return fis.readAllBytes();
    }

    @GetMapping(value = "/users/{id}",produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] downloadImage(@PathVariable("id") long userId) throws Exception {
        String imageUrl = this.dao.getUserImageUrl(userId);
        return getImage(imageUrl);
    }

    @GetMapping(value = "/products/{id}",produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] downloadProductImage(@PathVariable("id") long id) throws Exception {
        String imageUrl = this.dao.getProductImageUrl(id);
        return getImage(imageUrl);
    }

}