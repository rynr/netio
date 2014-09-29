netio
=====

NETIO is a series of smart sockets nd PDU (Power Distribution Unit) by
[Koukaam](http://www.koukaam.se/kkm/index.php).

This library gives you a Java-Interface to control them.

Example
-------

```java
NetworkSwitch networkSwitch = new NetworkSwitch.Builder("switch.host.name", 2345)
        .setUsername("admin").setPassword("secret").build();
// Switch all 4 units off (1 on, 0 off, i reset, u no-change)
networkSwitch.send("0000");
```

Info
----

 - [Info](https://rynr.github.io/netio/)
 - [Github](https://github.com/rynr/netio)
 - [Bugs](https://github.com/rynr/netio/issues)
 - [![Build Status](https://travis-ci.org/rynr/netio.svg?branch=master)](https://travis-ci.org/rynr/netio)

