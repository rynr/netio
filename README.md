netio
=====

NETIO is a series of smart sockets nd PDU (Power Distribution Unit) by
[Koukaam](http://www.koukaam.se/kkm/index.php).

This library gives you a Java-Interface to control them.

Include
-------

To use this library (in the current state), you have to build it yourself. Once
the next release is done, you can use it via maven, gradle, ….

The next release includes support for java 9 modules. You require
`org.rjung.util.netio`.

Current State
-------------

This library is in a non stable state at the moment. The library on maven
central can be used and uses a socket connection. The current main branch has
changed to use a `http` connection, but it's not done yet.

Example
-------

```java
NetworkSwitch networkSwitch = NetworkSwitch.builder("switch.host.name", 2345)
        .username("admin").password("secret").build();
// Switch all 4 units off (1 on, 0 off, i reset, u no-change)
networkSwitch.set(1, Switch.ON);
```

Info
----

 - [Info](https://rynr.github.io/netio/)
 - [Github](https://github.com/rynr/netio)
 - [Bugs](https://github.com/rynr/netio/issues)
 - [![Join the chat at https://gitter.im/rynr/netio](https://badges.gitter.im/rynr/netio.svg)](https://gitter.im/rynr/netio)
 - [![Build Status](https://github.com/rynr/netio/actions/workflows/maven.yml/badge.svg?branch=master)](https://travis-ci.org/rynr/netio)

