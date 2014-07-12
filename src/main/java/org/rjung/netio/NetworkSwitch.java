package org.rjung.netio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkSwitch {

	private static final String CHARSET_NAME = "US-ASCII";
	private static final Logger LOG = LoggerFactory
			.getLogger(NetworkSwitch.class);

	private String hostname;
	private Integer port;
	private String username;
	private String password;

	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;

	private String hash;

	private NetworkSwitch(Builder builder) {
		this.hostname = builder.hostname;
		this.port = builder.port;
		this.username = builder.username;
		this.password = builder.password;
	}

	public void send(String lights) throws NetIOException {
		LOG.trace("send(" + lights + ")");
		if (!lights.matches("^[01iu]{4}$")) {
			String message = "Invalid send-Format (" + lights + ")";
			LOG.debug(message);
			throw new NetIOException(message);
		}
		try {
			if (!isConnected()) {
				login();
			}
			String sendString = "port list " + lights;
			LOG.debug("> " + sendString);
			writer.write(sendString);
			writer.newLine();
			writer.flush();
			StringBuffer response = new StringBuffer();
			while (reader.ready()) {
				response.append(reader.read());
			}
			LOG.debug("< " + response.toString());
		} catch (IOException e) {
			try {
				socket.close();
			} catch (IOException e1) {
				LOG.error(
						"Error closing Socket on Exception (" + e.getMessage(),
						e1);
			}
			throw new NetIOException(e);
		}
	}

	private boolean isConnected() {
		LOG.trace("isConnected()");
		return (socket != null && socket.isConnected() && socket.isBound() && !socket
				.isClosed());
	}

	private void login() {
		LOG.trace("login()");
		try {
			if (!isConnected()) {
				loginConnect();
				loginSendCredentials();
			}
		} catch (NetIOException e) {
			LOG.error("Could not connect to " + hostname + ":" + port + ": "
					+ e.getMessage());
		}
	}

	private void loginSendCredentials() throws NetIOException {
		LOG.trace("loginSendCredentials()");
		try {
			String sendString = "clogin " + username + " " + getPassword();
			LOG.debug("> " + sendString);
			writer.write(sendString);
			writer.newLine();
			writer.flush();
			String response = reader.readLine();
			LOG.debug("< " + response);
			if (response == null || !response.startsWith("250")) {
				throw new IOException(response);
			}
		} catch (NoSuchAlgorithmException e) {
			throw new NetIOException(e);
		} catch (IOException e) {
			throw new NetIOException(e);
		}
	}

	private String getPassword() throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		LOG.trace("getPassword()");
		MessageDigest digest = MessageDigest.getInstance("MD5");
		return DatatypeConverter.printHexBinary(digest.digest((username
				+ password + hash).getBytes(CHARSET_NAME)));
	}

	private void loginConnect() throws NetIOException {
		LOG.trace("loginConnect()");
		try {
			socket = new Socket(hostname, port);
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream(), CHARSET_NAME));
			writer = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), CHARSET_NAME));
			String response = reader.readLine();
			LOG.debug("< " + response);
			if (response != null && response.startsWith("100")) {
				hash = response.substring(10, 18);
				LOG.debug("Got Hash: " + hash);
			}
		} catch (UnknownHostException e) {
			throw new NetIOException(e);
		} catch (IOException e) {
			throw new NetIOException(e);
		}
	}

	public static class Builder {
		private String hostname;
		private Integer port;
		private String username;
		private String password;

		public Builder(String hostname, Integer port) {
			this.hostname = hostname;
			this.port = port;
		}

		public Builder setUsername(String username) {
			this.username = username;
			return this;
		}

		public Builder setPassword(String password) {
			this.password = password;
			return this;
		}

		public NetworkSwitch build() {
			return new NetworkSwitch(this);
		}
	}
}
