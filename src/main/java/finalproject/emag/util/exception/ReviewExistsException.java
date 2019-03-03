package finalproject.emag.util.exception;

public class ReviewExistsException extends BaseException {

    public ReviewExistsException() {
        super("You already have review on this product");
    }

}
