package won.ecommerce.exception;

public class VerifyIamportException extends RuntimeException{
    public VerifyIamportException() {super();}

    public VerifyIamportException(String message) {
        super(message);
    }

    public VerifyIamportException(String message, Throwable cause) {
        super(message, cause);
    }

    public VerifyIamportException(Throwable cause) {
        super(cause);
    }
}
