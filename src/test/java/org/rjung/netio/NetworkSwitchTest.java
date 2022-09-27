package org.rjung.netio;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NetworkSwitchTest {

    private Random random;

    @Mock
    private HttpClient httpClient;

    @BeforeEach
    void setup() {
        synchronized (this) {
            if (random == null) {
                this.random = new SecureRandom();
            }
        }
    }

    @Test
    void builderRequiresHostname() {
        assertThrows(NullPointerException.class, () -> NetworkSwitch.builder(null));
    }

    @Test
    void builderWithPortRequiresHostname() {
        assertThrows(NullPointerException.class, () -> NetworkSwitch.builder(null, 123));
    }

    @Test
    void builderGeneratesSwitchWhichIsNeitherConnectedNorAuthenticated() throws IOException {
        var networkSwitch = NetworkSwitch.builder("hostname").build();

        assertThat(networkSwitch.isConnected(), is(false));
        assertThat(networkSwitch.isAuthenticated(), is(false));
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
                hasProperty("state", is(NetworkSwitch.State.DISCONNECTED))
        ));
        assertThat(getPrivateField(networkSwitch, "password"), is(password));
        assertThat(getPrivateField(networkSwitch, "username"), is(username));
    }

    private Object getPrivateField(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        var field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
}
