package com.friendscout24.netio;


public class NetIOException extends Exception {
	public NetIOException(String message) {
		super(message);
	}

	public NetIOException(Throwable e) {
		super(e);
	}

	private static final long serialVersionUID = -1948688832753938552L;
}
