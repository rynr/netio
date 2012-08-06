package com.friendscout24.netio;


import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class NetworkSwitchTest {

    @Before
    public void setUp() throws Exception {
    }


    @Test
    public void testBytesToHexStringDoesSomeCorrectConversions() {
        assertEquals("6162636465666768696a6b6c6d6e6f707172737475767778797a",
                NetworkSwitch.bytesToHexString("abcdefghijklmnopqrstuvwxyz".getBytes()));
    }

}
