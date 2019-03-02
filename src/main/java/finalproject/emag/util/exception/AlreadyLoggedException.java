package finalproject.emag.util.exception;

public class AlreadyLoggedException extends BaseException{
    public AlreadyLoggedException() {
        super("You are already logged in.");
    }
}
