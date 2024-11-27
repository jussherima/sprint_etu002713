package exception;

import java.lang.reflect.Field;

public class ParameterRequiredException extends Exception {

    String param_name;

    public ParameterRequiredException(String message) {
        super(message);
    }

    public ParameterRequiredException(Field[] liste_field) {

    }
}
