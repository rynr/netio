package org.rjung.netio;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.rjung.netio.NetworkSwitch.State;

public class NetworkSwitchTest {

	private NetworkSwitch networkSwitch;

	@Before
	public void setUp() throws Exception {
		networkSwitch = new NetworkSwitch.Builder("hostname", 2345)
				.setUsername("username").setPassword("password").build();
	}

	@Test
	public void newNetworkSwitchIsDisconnected() {
		assertThat(networkSwitch.state, equalTo(State.DISCONNECTED));
	}

}
