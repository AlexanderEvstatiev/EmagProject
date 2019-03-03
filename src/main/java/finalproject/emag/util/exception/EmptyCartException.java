package finalproject.emag.util.exception;

public class EmptyCartException extends BaseException {

    public EmptyCartException() {
        super("Your cart is empty.");
    }
}
