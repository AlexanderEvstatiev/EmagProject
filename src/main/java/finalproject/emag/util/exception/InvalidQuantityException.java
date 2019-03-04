package finalproject.emag.util.exception;
public class InvalidQuantityException extends BaseException {

    public InvalidQuantityException() {
        super("You can't change the quantity to this value!");
    }
}
