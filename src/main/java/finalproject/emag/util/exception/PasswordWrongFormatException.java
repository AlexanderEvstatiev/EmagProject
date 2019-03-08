package finalproject.emag.util.exception;

public class PasswordWrongFormatException extends BaseException{
    public PasswordWrongFormatException() {
        super("Password must be at least 8 symbols and have no spaces");
    }
}
