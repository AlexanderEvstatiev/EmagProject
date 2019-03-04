package finalproject.emag.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.emag.model.dao.ProductDao;
import finalproject.emag.model.dao.UserDao;
import finalproject.emag.model.pojo.Product;
import finalproject.emag.model.pojo.User;
import finalproject.emag.util.exception.ImageMissingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Base64;

@RestController
public class ImageController extends BaseController{

    private static final String IMAGE_PATH = "C:\\Users\\rache\\Desktop\\users\\";
    private static final String IMAGE_PRODUCT_PATH = "C:\\Users\\rache\\Desktop\\products\\";

    @Autowired
    private UserDao userDao;
    private ProductDao productDao;
    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/images")
    public String uploadImage(@RequestBody String input, HttpSession session) throws Exception {
        validateLogin(session);
        User user = (User) session.getAttribute("user");
        JsonNode jsonNode = objectMapper.readTree(input);
        String base64 = jsonNode.get("image_url").textValue();
        byte[] bytes = Base64.getDecoder().decode(base64);
        String name = user.getId()+System.currentTimeMillis()+".png";
        File image = new File(IMAGE_PATH +name);
        FileOutputStream fos = new FileOutputStream(image);
        fos.write(bytes);
        user.setImageUrl(name);
        this.userDao.uploadImage(user,user.getImageUrl());
        fos.close();
        return "Image upload successful";
    }

    @GetMapping(value = "/images/users/{id}",produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] downloadImage(@PathVariable("id") long userId) throws Exception {
        String imageUrl = this.userDao.getImageUrl(userId);
        if(imageUrl==null){
            throw new ImageMissingException();
        }
        File image = new File(IMAGE_PATH+imageUrl);
        FileInputStream fis = new FileInputStream(image);
        return fis.readAllBytes();
    }

    @GetMapping(value = "/images/products/{id}",produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] downloadProductImage(@PathVariable("id") long id, HttpSession session) throws Exception {
        Product p = productDao.getProductById(id);
        File image = new File(IMAGE_PRODUCT_PATH + p.getImageUrl());
        FileInputStream fis = new FileInputStream(image);
        return fis.readAllBytes();
    }

}