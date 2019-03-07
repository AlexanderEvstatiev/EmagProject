package finalproject.emag.util.exception;

public class EmailTakenException extends BaseException{
    public EmailTakenException() {
        super("Email is taken");
    }
}
