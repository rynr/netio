package org.rjung.netio;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rjung.netio.NetworkSwitch.State;

public class NetworkSwitchTest {

	private NetworkSwitch networkSwitch;
	@Mock
	private BufferedWriter writer;
	@Mock
	private BufferedReader reader;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		networkSwitch = new NetworkSwitch.Builder("hostname", 2345)
				.setUsername("username").setPassword("password").build();
		networkSwitch.writer = writer;
		networkSwitch.reader = reader;
	}

	@Test
	public void newNetworkSwitchIsDisconnected() {
		assertThat(networkSwitch.state, equalTo(State.DISCONNECTED));
	}

	@Test(expected = NetIOException.class)
	public void sendNullStringRaisesException() throws NetIOException {
		networkSwitch.send(null);
	}

	@Test(expected = NetIOException.class)
	public void sendInvalidStringRaisesException() throws NetIOException {
		networkSwitch.send("invalid");
	}

	@Test
	public void validSendStringWritesToConnection() throws NetIOException, IOException {
		networkSwitch.state = State.AUTHORIZED;
		networkSwitch.send("01iu");
		verify(writer).write("port list 01iu");
	}
}
