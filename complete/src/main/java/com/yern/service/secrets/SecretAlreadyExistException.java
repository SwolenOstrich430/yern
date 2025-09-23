public class SecretAlreadyExistException extends RuntimeException {
    public SecretAlreadyExistException(String message) {
        super(message);
    }
}