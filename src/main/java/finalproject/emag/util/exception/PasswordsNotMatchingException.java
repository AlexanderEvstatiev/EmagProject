package finalproject.emag.util.exception;

public class PasswordsNotMatchingException extends BaseException{
    public PasswordsNotMatchingException() {
        super("Passwords does not match");
    }
}
