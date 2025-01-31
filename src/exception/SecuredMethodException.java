package exception;

import java.lang.reflect.Method;

public class SecuredMethodException extends Exception {
    Method m;

    public SecuredMethodException(Method m, String message) {
        super(message);
        this.m = m;
    }

    public Method getM() {
        return m;
    }

    public void setM(Method m) {
        this.m = m;
    }

}
