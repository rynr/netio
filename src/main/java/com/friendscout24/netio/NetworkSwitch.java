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

import com.google.inject.Inject;
import com.google.inject.name.Named;


public class NetworkSwitch {

    @Inject
    @Named("PowerSwitch Hostname")
    private String         hostname;
    @Inject
    @Named("PowerSwitch Port")
    private Integer        port;
    @Inject
    @Named("PowerSwitch Username")
    private String         username;
    @Inject
    @Named("PowerSwitch Password")
    private String         password;
    private Socket         socket;
    private Logger         logger;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String         hash;


    @Inject
    public NetworkSwitch() {
        logger = LoggerFactory.getLogger(NetworkSwitch.class);
    }


    public void send(String lights) throws NetIOException {
        if (!lights.matches("^[01iu]{4}$"))
            throw new NetIOException("Invalid Format");
        try {
            if (socket == null || !socket.isConnected()) {
                login();
            }
            writer.write("port list " + lights);
            writer.newLine();
            writer.flush();
            StringBuffer response = new StringBuffer();
            while (reader.ready()) {
                response.append(reader.read());
            }
            logger.debug(response.toString());
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e1) {
                logger.error("Error closing Socket on Exception (" + e.getMessage(), e1);
            }
            throw new NetIOException(e);
        }
    }


    private void login() {
        logger.debug("login()");
        try {
            if (socket == null || !socket.isConnected()) {
                loginConnect();
                loginSendCredentials();
            }
        } catch (NetIOException e) {
            logger.error("Could not connect to " + hostname + ":" + port + ": " + e.getMessage());
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
        return bytesToHexString(digest.digest((username + password + hash).getBytes()));
    }


    private void loginConnect() throws NetIOException {
        logger.debug("connect");
        try {
            socket = new Socket(hostname, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String line = reader.readLine();
            if (line.startsWith("100")) {
                hash = line.substring(10, 18);
                logger.debug("Got Hash: " + hash);
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

}
