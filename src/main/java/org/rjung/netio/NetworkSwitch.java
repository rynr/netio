package org.rjung.netio;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkSwitch {

    private static final byte[] HEX_ARRAY = "0123456789abcdef".getBytes();
    private static final Logger LOG = LoggerFactory.getLogger(NetworkSwitch.class);

    private final String username;
    private final String password;
    private final HttpClient client;
    private final URL baseUrl;

    private String hash;
    private String session;
    private State state;

    private NetworkSwitch(Builder builder) throws IOException {
        this.state = State.DISCONNECTED;
        this.username = builder.username;
        this.password = builder.password;
        this.client = HttpClient.newHttpClient();
        baseUrl = new URL("http", builder.hostname, builder.port, "/");
    }

    private boolean isConnected() {
        LOG.trace("isConnected()");
        return State.CONNECTED.equals(state);
    }

    private boolean isAuthorized() {
        LOG.trace("isAuthorized()");
        return State.AUTHORIZED.equals(state);
    }

    public void connect() throws URISyntaxException, IOException, InterruptedException {
        String[] elements = call(
                new URL(baseUrl, MessageFormat.format("/cgi/kshell.cgi?session=init+{0}", System.currentTimeMillis())))
                        .split(" ");
        if ("250".equals(elements[0])) {
            for (int i = 1; i < elements.length; i++) {
                String[] parts = elements[i].split("=");
                if ("hash".equals(parts[0])) {
                    this.hash = parts[1];
                } else if ("ssid".equals(parts[0])) {
                    this.session = parts[1];
                } else {
                    LOG.warn("Unknown property {}={}", parts[0], parts[1]);
                }
            }
            this.state = State.CONNECTED;
        }
    }

    public void authorize() throws URISyntaxException, IOException, InterruptedException,
            NoSuchAlgorithmException {
        if (!isConnected()) {
            connect();
        }
        String[] elements = call(new URL(baseUrl,
                MessageFormat.format("/cgi/kshell.cgi?session=ssid+{0}&cmd=clogin+{1}+{2}", session, username, bytesToHex(
                        MessageDigest.getInstance("MD5").digest((username + password + hash).getBytes(StandardCharsets.US_ASCII))))))
                                .split(" ");
        if ("250".equals(elements[0])) {
            this.state = State.AUTHORIZED;
        }
    }

    public void set(int light, int state) throws NoSuchAlgorithmException, URISyntaxException,
            IOException, InterruptedException {
        if (!isAuthorized()) {
            authorize();
        }
        String[] elements = call(new URL(baseUrl,
                MessageFormat.format("/cgi/kshell.cgi?session=ssid+{0}&cmd=port+{1}+{2}", this.session, light, state)))
                        .split(" ");
        if ("250".equals(elements[0])) {
            LOG.debug("OK");
        }
    }

    private String call(URL url) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(url.toURI()).GET().build();
        return client.send(httpRequest, BodyHandlers.ofString()).body();
    }

    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    public enum State {
        DISCONNECTED, CONNECTED, AUTHORIZED
    }

    public static class Builder {
        private final String hostname;
        private final Integer port;
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

        public NetworkSwitch build() throws IOException {
            return new NetworkSwitch(this);
        }
    }
}
