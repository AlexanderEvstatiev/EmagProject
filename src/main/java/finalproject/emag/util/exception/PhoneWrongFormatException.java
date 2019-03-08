package finalproject.emag.util.exception;

public class PhoneWrongFormatException extends BaseException{
    public PhoneWrongFormatException() {
        super("Not a valid phone number");
    }
}
