package finalproject.emag.util.exception;

public class WrongSearchWordException extends Exception {

    public WrongSearchWordException() {
        super("There are not products matching the search field!");
    }
}
