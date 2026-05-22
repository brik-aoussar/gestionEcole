package exception;

public class ValidationException extends RuntimeException {
    private final String champ;

    public ValidationException(String champ, String message) {
        super(message);
        this.champ = champ;
    }

    public String getChamp() { return champ; }
}
