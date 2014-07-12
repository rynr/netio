package org.rjung.netio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkSwitch {

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
		if (!lights.matches("^[01iu]{4}$"))
			throw new NetIOException("Invalid Format");
		try {
			if (!isConnected()) {
				login();
			}
			writer.write("port list " + lights);
			writer.newLine();
			writer.flush();
			StringBuffer response = new StringBuffer();
			while (reader.ready()) {
				response.append(reader.read());
			}
			LOG.debug(response.toString());
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
		return (socket != null && socket.isConnected() && socket.isBound() && !socket
				.isClosed());
	}

	private void login() {
		LOG.debug("login()");
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
		try {
			writer.write("clogin " + username + " " + getPassword());
			writer.newLine();
			writer.flush();
			String line = reader.readLine();
			if (!line.startsWith("250")) {
				throw new IOException(line);
			}
		} catch (NoSuchAlgorithmException e) {
			throw new NetIOException(e);
		} catch (IOException e) {
			throw new NetIOException(e);
		}
	}

	private String getPassword() throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		return bytesToHexString(digest.digest((username + password + hash)
				.getBytes()));
	}

	private void loginConnect() throws NetIOException {
		LOG.debug("connect");
		try {
			socket = new Socket(hostname, port);
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			String line = reader.readLine();
			if (line.startsWith("100")) {
				hash = line.substring(10, 18);
				LOG.debug("Got Hash: " + hash);
			}
		} catch (UnknownHostException e) {
			throw new NetIOException(e);
		} catch (IOException e) {
			throw new NetIOException(e);
		}
	}

	public static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);

		Formatter formatter = new Formatter(sb);
		for (byte b : bytes) {
			formatter.format("%02x", b);
		}
		formatter.close();

		return sb.toString();
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
