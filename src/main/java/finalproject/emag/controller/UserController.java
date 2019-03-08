package finalproject.emag.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalproject.emag.model.dao.UserDao;
import finalproject.emag.model.dto.EditEmailDto;
import finalproject.emag.model.dto.EditPasswordDto;
import finalproject.emag.model.dto.EditPersonalInfoDto;
import finalproject.emag.model.dto.GetUserDto;
import finalproject.emag.model.pojo.User;
import finalproject.emag.model.pojo.messages.MessageSuccess;
import finalproject.emag.util.GetDate;
import finalproject.emag.util.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping(value = "/users",produces = "application/json")
public class UserController extends BaseController {

    private static final String EDIT_SUCCESSFUL = "Edit successful";
    private static final String USER = "user";

    @Autowired
    private UserDao dao;
    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/login")
    public GetUserDto loginUser(@RequestBody String input, HttpSession session) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(input);
        if (!jsonNode.has("email") || !jsonNode.has("password")) {
            throw new MissingValuableFieldsException();
        }
        String email = jsonNode.get("email").textValue();
        String password = jsonNode.get("password").textValue();
        User user = this.dao.getUserByEmail(email);
        if (user == null) {
            throw new WrongCredentialsException();
        }
        if(!BCrypt.checkpw(password,user.getPassword())){
            throw new WrongCredentialsException();
        }
        else {
            session.setAttribute(USER, user);
            session.setAttribute("email", user.getEmail());
            session.setMaxInactiveInterval((60 * 60));
            return getLoggedUserInfo(user);
        }
    }

    private GetUserDto getLoggedUserInfo(User user){
        return new GetUserDto(user.getId(),user.getEmail(),user.getName(),user.getUsername(),
                user.getPhoneNumber(),user.getBirthDate(),user.isSubscribed(),user.getImageUrl());
    }
    private String checkIfNull(JsonNode node, String input) {
        if (node.has(input)) {
            return node.get(input).textValue();
        }
        return null;
    }
    private void checkPhone(String phone) throws PhoneWrongFormatException {
        String phoneRegex = "08[789]\\d{7}";
        if (phone != null) {
            if (!phone.matches(phoneRegex)) {
                throw new PhoneWrongFormatException();
            }
        }
    }
    private void checkEmail(String email) throws EmailInvalidFormatException {
        String emailRegex = "([A-Za-z0-9-_.]+@[A-Za-z0-9-_]+(?:\\.[A-Za-z]+)+)";
        if (!email.matches(emailRegex)) {
            throw new EmailInvalidFormatException();
        }
    }
    private void checkPassword(String password) throws PasswordWrongFormatException {
        String passwordRegex = "^(?=\\S+$).{8,}$";
        if(password != null) {
            if (!password.matches(passwordRegex)) {
                throw new PasswordWrongFormatException();
            }
        }
    }
    private User getUser(JsonNode node) throws ParseException, BaseException{
        String email = node.get("email").textValue();
        checkEmail(email);
        String password = checkIfNull(node, "password");
        checkPassword(password);
        String fullName = node.get("full_name").textValue();
        String username = checkIfNull(node, "username");
        String phone = checkIfNull(node, "phone");
        checkPhone(phone);
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
        return new User(email, password, fullName, username, phone, date, subscribed, admin, imageUrl);
    }

    @PostMapping(value = "/register")
    public GetUserDto registerUser(@RequestBody String input, HttpSession session)
            throws Exception {
        JsonNode jsonNode = objectMapper.readTree(input);
        if (!jsonNode.has("email") || !jsonNode.has("password") ||
                !jsonNode.has("password2") ||
                !jsonNode.has("full_name") || !jsonNode.has("subscribed")) {
            throw new MissingValuableFieldsException();
        }
        User user = getUser(jsonNode);
        if (this.dao.checkIfEmailIsFree(user.getEmail())) {
            if (dao.checkIfUsernameExists(user.getUsername())) {
                if (!user.getPassword().equals(jsonNode.get("password2").textValue())) {
                    throw new PasswordsNotMatchingException();
                }
                this.dao.addUser(user);
                session.setAttribute(USER, user);
                session.setAttribute("id", user.getId());
                session.setMaxInactiveInterval((60 * 60));
                return getLoggedUserInfo(user);
            } else {
                throw new UsernameTakenException();
            }
        } else {
            throw new EmailTakenException();
        }
    }

    @PostMapping(value = "/logout")
    public MessageSuccess logoutUser(HttpSession session) throws NotLoggedException {
        validateLogin(session);
        session.invalidate();
        return new MessageSuccess("You logged out", LocalDateTime.now());
    }

    @PutMapping(value = "/subscribe")
    public MessageSuccess subscribe(HttpSession session) throws SQLException, BaseException {
        validateLogin(session);
        return this.dao.subscribe((User)session.getAttribute(USER));
    }

    @PutMapping(value = "/unsubscribe")
    public MessageSuccess unsubscribe(HttpSession session) throws SQLException, BaseException {
        validateLogin(session);
        return this.dao.unsubscribe((User)session.getAttribute(USER));
    }

    @PutMapping(value = "/edit-personal-info")
    public MessageSuccess editPersonalInfoUser(@RequestBody String input,HttpSession session)
            throws Exception{
        validateLogin(session);
        JsonNode jsonNode = objectMapper.readTree(input);
        if(!jsonNode.has("full_name")){
            throw new MissingValuableFieldsException();
        }
        String birthDate = jsonNode.get("birth_date").textValue();
        LocalDate date = GetDate.getDate(birthDate);
        EditPersonalInfoDto user = new EditPersonalInfoDto(jsonNode.get("full_name").textValue(),
                checkIfNull(jsonNode,"username"),
                checkIfNull(jsonNode,"phone"),
                date);
        checkPhone(user.getPhoneNumber());
        if(this.dao.checkIfUsernameExists(user.getUsername())) {
            User loggedUser = (User)session.getAttribute(USER);
            this.dao.editPersonalInfoUser(user,loggedUser.getId());
            return new MessageSuccess(EDIT_SUCCESSFUL,LocalDateTime.now());
        }
        else{
            throw new UsernameTakenException();
        }
    }

    @PutMapping(value = "/edit-email")
    public MessageSuccess editUserSecurity(@RequestBody String input,HttpSession session)
            throws Exception{
        validateLogin(session);
        JsonNode jsonNode = objectMapper.readTree(input);
        if(!jsonNode.has("email") || !jsonNode.has("password")){
            throw new MissingValuableFieldsException();
        }
        if(!dao.checkIfEmailIsFree(jsonNode.get("email").textValue())){
            throw new EmailTakenException();
        }
        User loggedUser = (User)session.getAttribute(USER);
        String email = jsonNode.get("email").textValue();
        checkEmail(email);
        String pass = jsonNode.get("password").textValue();
        if(!BCrypt.checkpw(pass,loggedUser.getPassword())){
            throw new WrongCredentialsException();
        }
        EditEmailDto user = new EditEmailDto(email);
        this.dao.editEmail(user,loggedUser.getId());
        return new MessageSuccess(EDIT_SUCCESSFUL,LocalDateTime.now());
    }

    @PutMapping(value = "/edit-password")
    public MessageSuccess editPassword(@RequestBody String input,HttpSession session,HttpServletResponse response)
            throws Exception {
        JsonNode jsonNode = objectMapper.readTree(input);
        validateLogin(session);
        if (!jsonNode.has("current_password") || !jsonNode.has("password") ||
                !jsonNode.has("password2")) {
            response.setStatus(404);
            throw new MissingValuableFieldsException();
        }
        User loggedUser = (User)session.getAttribute(USER);
        String currentPass = jsonNode.get("current_password").textValue();
        String pass = jsonNode.get("password").textValue();
        checkPassword(pass);
        String pass2 = jsonNode.get("password2").textValue();
        if(!BCrypt.checkpw(currentPass,loggedUser.getPassword())){
            throw new WrongCredentialsException();
        }
        if (!pass.equals(pass2)) {
            throw new PasswordsNotMatchingException();
        }
        EditPasswordDto user = new EditPasswordDto(pass);
        this.dao.editPassword(user,loggedUser.getId());
        return new MessageSuccess(EDIT_SUCCESSFUL,LocalDateTime.now());
    }
}