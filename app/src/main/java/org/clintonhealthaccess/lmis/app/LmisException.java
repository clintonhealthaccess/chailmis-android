package org.clintonhealthaccess.lmis.app;

public class LmisException extends RuntimeException {
    public LmisException(Throwable cause) {
        super(cause);
    }

    public LmisException() {
        super("");
    }

    public LmisException(String message) {
        super(message);
    }

    public LmisException(String message, Throwable cause) {
        super(message, cause);
    }
}
