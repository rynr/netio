package org.rjung.netio;

public class NetIOException extends Exception {

    public NetIOException(String message) {
        super(message);
    }

    public NetIOException(Throwable e) {
        super(e);
    }

    public NetIOException(String message, Throwable e) {
        super(message, e);
    }

    private static final long serialVersionUID = -1948688832753938552L;
}
