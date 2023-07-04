package nl.ing.lovebird.user.exception;

public class OneOffUserException extends RuntimeException {

    public OneOffUserException(final String message) {
        super(message);
    }
}
