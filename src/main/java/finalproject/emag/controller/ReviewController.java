package finalproject.emag.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.emag.model.dao.ReviewDao;
import finalproject.emag.model.dto.DeleteReviewDto;
import finalproject.emag.model.dto.ReviewDto;
import finalproject.emag.model.pojo.User;
import finalproject.emag.util.exception.MissingValuableFieldsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/products",produces = "application/json")
public class ReviewController extends BaseController{

    private static final String USER = "user";

    @Autowired
    private ReviewDao dao;
    private ObjectMapper objectMapper = new ObjectMapper();

    private ReviewDto getReview(String input, long productId, HttpServletRequest request) throws Exception {
        JsonNode jsonNode = this.objectMapper.readTree(input);
        if(!jsonNode.has("title") || !jsonNode.has("comment") || !jsonNode.has("grade")){
            throw new MissingValuableFieldsException();
        }
        validateLogin(request.getSession());
        User user = (User)request.getSession().getAttribute(USER);
        return new ReviewDto(user.getId(),productId,jsonNode.get("title").textValue(),
                jsonNode.get("comment").textValue(),jsonNode.get("grade").intValue());
    }

    @PostMapping(value = "/{id}")
    public String addReview(@RequestBody String input, @PathVariable("id")long productId, HttpServletRequest request) throws Exception {
        ReviewDto review = getReview(input,productId,request);
        this.dao.addReview(review);
        return "Review added";
    }

    @DeleteMapping(value = "/{id}")
    public String deleteReview(@PathVariable("id")long productId,HttpServletRequest request) throws Exception {
        validateLogin(request.getSession());
        User user = (User)request.getSession().getAttribute("user");
        DeleteReviewDto review = new DeleteReviewDto(user.getId(),productId);
        this.dao.deleteReview(review);
        return "Review deleted";
    }

    @PutMapping(value = "/{id}")
    public String editReview(@RequestBody String input, @PathVariable("id")long productId, HttpServletRequest request) throws Exception {
        ReviewDto review = getReview(input,productId,request);
        this.dao.editReview(review);
        return "Review updated";
    }

}