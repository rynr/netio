package org.rjung.netio.api;

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

}
