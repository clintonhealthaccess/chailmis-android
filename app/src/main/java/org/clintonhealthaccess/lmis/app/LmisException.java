package org.clintonhealthaccess.lmis.app;

public class LmisException extends RuntimeException {
    public LmisException(Throwable e) {
        super(e);
    }

    public LmisException() {
        super("");
    }

    public LmisException(String s) {
        super(s);
    }

    public LmisException(String s, Exception e) {
        super(s, e);
    }
}
