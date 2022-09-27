package org.rjung.netio.api;

public class NetIOException extends Exception {
    
    private static final long serialVersionUID = -1948688832753938552L;

    public NetIOException(String message) {
        super(message);
    }

    public NetIOException(Throwable e) {
        super(e);
    }

    public NetIOException(String message, Throwable e) {
        super(message, e);
    }
}
