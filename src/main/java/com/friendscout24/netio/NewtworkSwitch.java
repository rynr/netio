package com.friendscout24.netio;

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

public class NewtworkSwitch {
	private String hostname;
	private int port;
	private String username;
	private String password;
	private Socket socket;
	private Logger logger;
	private BufferedReader reader;
	private BufferedWriter writer;
	private String hash;
	private static State state;

	public NewtworkSwitch(String host, int port, String username,
			String password) throws UnknownHostException, IOException {
		logger = LoggerFactory.getLogger(NewtworkSwitch.class);
		this.hostname = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	public void noop() throws IOException {
		// Only sending command if authorized
		if (state == State.AUTHORIZED) {
			writer.write("noop");
			writer.newLine();
			writer.flush();
			// There's no response for noop
		}
	}

	public void send(String lights) throws IOException, NetIOException {
		if (!lights.matches("^[01iu]{4}$"))
			throw new NetIOException("Invalid Format");
		if (state != State.AUTHORIZED) {
			login();
		}
		writer.write("port list " + lights);
		writer.newLine();
		writer.flush();
		logger.debug(reader.readLine());
	}

	private State login() {
		State result = State.DISCONNECTED;
		logger.debug("login()");
		try {
			if (socket == null || !socket.isConnected()) {
				result = loginConnect();
				if (result == State.CONNECTED)
					result = loginSendCredentials();
			}
		} catch (UnknownHostException e) {
			logger.error("Could not connect to " + hostname + ":" + port + ": "
					+ e.getMessage());
		} catch (IOException e) {
			logger.error("Could not connect to " + hostname + ":" + port + ": "
					+ e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			logger.error("Could not connect to " + hostname + ":" + port + ": "
					+ e.getMessage());
		}
		return result;
	}

	private State loginSendCredentials() throws NoSuchAlgorithmException,
			IOException {
		State result = State.CONNECTED;
		writer.write("clogin " + username + " " + getPassword());
		writer.newLine();
		writer.flush();
		String line = reader.readLine();
		if (line.startsWith("250")) {
			result = State.AUTHORIZED;
		}
		return result;
	}

	private String getPassword() throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		return bytesToHexString(digest.digest((username + password + hash)
				.getBytes()));
	}

	private State loginConnect() throws UnknownHostException, IOException {
		logger.debug("connect");
		State result = State.DISCONNECTED;
		socket = new Socket(hostname, port);
		reader = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream()));
		String line = reader.readLine();
		if (line.startsWith("100")) {
			hash = line.substring(10, 18);
			logger.debug("Got Hash: " + hash);
			result = State.CONNECTED;
		}
		return result;
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
}
