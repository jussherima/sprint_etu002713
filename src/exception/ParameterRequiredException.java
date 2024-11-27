package exception;

public class ParameterRequiredException extends Exception {

    String param_name;

    public ParameterRequiredException(String message) {
        super(message);
    }

    public ParameterRequiredException(String class_name, String message) {
        super(message);
    }
}
