package org.rjung.netio.api;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rjung.netio.api.enums.ConnectionState;

@ExtendWith(MockitoExtension.class)
public class NetworkSwitchTest {

    private Random random;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<Object> httpResponse;

    @Captor
    private ArgumentCaptor<HttpRequest> httpRequestCaptor;

    @BeforeEach
    void setup() {
        synchronized (this) {
            if (random == null) {
                this.random = new SecureRandom();
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void builderRequiresHostname() {
        assertThrows(NullPointerException.class, () -> NetworkSwitch.builder(null));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void builderWithPortRequiresHostname() {
        assertThrows(NullPointerException.class, () -> NetworkSwitch.builder(null, 123));
    }

    @Test
    void builderGeneratesSwitchWhichIsNeitherConnectedNorAuthenticated() throws IOException {
        var networkSwitch = NetworkSwitch.builder("hostname").build();

        assertThat(networkSwitch.isConnected(), is(false));
        assertThat(networkSwitch.isAuthenticated(), is(false));
        assertThat(networkSwitch, hasProperty("state", is(ConnectionState.DISCONNECTED)));
    }

    @Test
    void builderGeneratesSwitchWithGivenPropertiesAndDefaults() throws IOException, NoSuchFieldException, IllegalAccessException {
        String hostname = UUID.randomUUID().toString();
        int port = random.nextInt(1024);
        String username = UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();
        var networkSwitch = NetworkSwitch
                .builder(hostname, port)
                .username(username)
                .password(password)
                .build();

        assertThat(networkSwitch, allOf(
                hasProperty("baseUrl", allOf(
                        hasProperty("protocol", is("http")),
                        hasProperty("host", is(hostname)),
                        hasProperty("port", is(port)),
                        hasProperty("path", is("/"))
                )),
                hasProperty("state", is(ConnectionState.DISCONNECTED))
        ));
        assertThat(getPrivateField(networkSwitch, "password"), is(password));
        assertThat(getPrivateField(networkSwitch, "username"), is(username));
    }

    @Test
    void baseUrlMatchesGivenParameters() throws IOException {
        String hostname = UUID.randomUUID().toString();
        int port = random.nextInt(1024);
        var networkSwitch = NetworkSwitch.builder(hostname, port).build();

        var baseUrl = networkSwitch.getBaseUrl();

        assertThat(baseUrl, allOf(
                hasProperty("host", is(hostname)),
                hasProperty("port", is(port))
        ));
    }

    @Test
    void connectInitializes() throws URISyntaxException, IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        when(httpResponse.body()).thenReturn("OK");
        var networkSwitch = getMockedNetworkSwitch();

        networkSwitch.connect();

        verify(httpClient).send(httpRequestCaptor.capture(), eq(HttpResponse.BodyHandlers.ofString()));
        var request = httpRequestCaptor.getValue();
        assertThat(request.uri().toString(), startsWith("http://hostname:80/cgi/kshell.cgi?session=init+"));
    }

    private NetworkSwitch getMockedNetworkSwitch() throws NoSuchFieldException, IllegalAccessException, IOException {
        var networkSwitch = NetworkSwitch.builder("hostname").build();
        setPrivateField(networkSwitch, "client", httpClient);
        return networkSwitch;
    }

    private Object getPrivateField(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        var field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        var field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

}
