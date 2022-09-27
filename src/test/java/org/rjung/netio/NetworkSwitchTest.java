package org.rjung.netio;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rjung.netio.NetworkSwitch.State;

public class NetworkSwitchTest {

    private NetworkSwitch networkSwitch;
    @Mock
    private BufferedWriter writer;
    @Mock
    private BufferedReader reader;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        networkSwitch = new NetworkSwitch.Builder("hostname", 2345)
                .setUsername("username").setPassword("password").build();
        networkSwitch.writer = writer;
        networkSwitch.reader = reader;
    }

    @Test
    void newNetworkSwitchIsDisconnected() {
        assertThat(networkSwitch.state, equalTo(State.DISCONNECTED));
    }

    @Test
    void sendNullStringRaisesException() throws NetIOException {
        var exception = Assertions.assertThrows(NetIOException.class,
                () -> networkSwitch.send(null) );
        assertThat(exception.getMessage(), is("TBD"));
    }

    @Test
    void sendInvalidStringRaisesException() throws NetIOException {
        var exception = Assertions.assertThrows(NetIOException.class,
                () -> networkSwitch.send("invalid") );
        assertThat(exception.getMessage(), is("TBD"));
    }

    @Test
    void validSendStringWritesToConnection() throws NetIOException,
            IOException {
        networkSwitch.state = State.AUTHORIZED;
        networkSwitch.send("01iu");
        verify(writer).write("port list 01iu");
    }
}
