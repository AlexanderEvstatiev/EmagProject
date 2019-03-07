package finalproject.emag.util.exception;

public class UsernameTakenException extends BaseException{
    public UsernameTakenException() {
        super("Username is taken");
    }
}
