package org.clintonhealthaccess.lmis.app.services;

public class ServiceException extends RuntimeException {
    public ServiceException(Throwable e) {
        super(e);
    }

    public ServiceException() {

    }
}
