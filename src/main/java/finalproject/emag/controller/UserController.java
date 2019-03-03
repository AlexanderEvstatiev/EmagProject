package finalproject.emag.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.emag.model.dao.UserDao;
import finalproject.emag.model.pojo.User;
import finalproject.emag.util.GetDate;
import finalproject.emag.util.exception.MissingValuableFieldsException;
import finalproject.emag.util.exception.NotLoggedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;

@RestController
@RequestMapping(value = "/users",produces = "application/json")
public class UserController extends BaseController {

    private static final String YOU_ARE_NOT_LOGGED_IN = "You are not logged in";
    private static final String WRONG_CREDENTIALS = "Wrong credentials";
    private static final String EDIT_SUCCESSFUL = "Edit successful";
    private static final String USER = "user";
    private static final String PASSWORDS_DOES_NOT_MATCH = "Passwords does not match";

    @Autowired
    private UserDao dao;
    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/login")
    public String loginUser(@RequestBody String input, HttpSession session) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(input);
        if (!jsonNode.has("email") || !jsonNode.has("password")) {
            throw new MissingValuableFieldsException();
        }
        String email = jsonNode.get("email").textValue();
        String password = jsonNode.get("password").textValue();
        User user = this.dao.getUserByEmail(email);
        if (user == null) {
            return WRONG_CREDENTIALS;
        }
        if (!password.equals(user.getPassword())) {
            return WRONG_CREDENTIALS;
        } else {
            validateAlreadyLogged(session);
            session.setAttribute(USER, user);
            session.setAttribute("email", user.getEmail());
            session.setMaxInactiveInterval((60 * 60));
            return "You logged in " + user.getName();
        }
    }

    private String checkIfNull(JsonNode node, String input) {
        if (node.has(input)) {
            return node.get(input).textValue();
        }
        return null;
    }

    private User getUser(JsonNode node, HttpServletResponse response) throws IOException, ParseException {
        String email = node.get("email").textValue();
        String password = checkIfNull(node, "password");
        String fullName = node.get("full_name").textValue();
        String username = checkIfNull(node, "username");
        String phone = checkIfNull(node, "phone");
        String birthDate = checkIfNull(node, "birth_date");
        String imageUrl = checkIfNull(node, "image_url");
        boolean subscribed = node.get("subscribed").asBoolean();
        boolean admin;
        if (node.get("admin") != null) {
            admin = node.get("admin").asBoolean();
        } else {
            admin = false;
        }
        LocalDate date = GetDate.getDate(birthDate);
        String phoneRegex = "08[789]\\d{7}";
        String emailRegex = "([A-Za-z0-9-_.]+@[A-Za-z0-9-_]+(?:\\.[A-Za-z]+)+)";
        if (!email.matches(emailRegex)) {
            response.setStatus(400);
            response.getWriter().append("Invalid email.");
        }
        if (phone != null) {
            if (!phone.matches(phoneRegex)) {
                response.setStatus(400);
                response.getWriter().append("Invalid phone number");
            }
        }
        return new User(email, password, fullName, username, phone, date, subscribed, admin, imageUrl);
    }

    @PostMapping(value = "/register")
    public String registerUser(@RequestBody String input, HttpServletResponse response, HttpSession session) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(input);
        if (!jsonNode.has("email") || !jsonNode.has("password") || !jsonNode.has("password2") ||
                !jsonNode.has("full_name") || !jsonNode.has("subscribed")) {
            throw new MissingValuableFieldsException();
        }
        User user = getUser(jsonNode, response);
        if (this.dao.checkIfEmailExists(user.getEmail())) {
            if (dao.checkIfUsernameExists(user.getUsername())) {
                if (!user.getPassword().equals(jsonNode.get("password2").textValue())) {
                    response.setStatus(400);
                    return PASSWORDS_DOES_NOT_MATCH;
                }
                this.dao.addUser(user);
                session.setAttribute(USER, user);
                session.setAttribute("id", user.getId());
                session.setMaxInactiveInterval((60 * 60));
                return "Register successful.";
            } else {
                response.setStatus(400);
                return "Username taken";
            }
        } else {
            response.setStatus(400);
            return "Email is taken.";
        }
    }
    @PostMapping(value = "/logout")
    public String logoutUser(HttpSession session) throws NotLoggedException {
        validateLogin(session);
        if(session.getAttribute(USER)!=null) {
            session.invalidate();
            return "You logged out";
        }
        return YOU_ARE_NOT_LOGGED_IN;
    }
}