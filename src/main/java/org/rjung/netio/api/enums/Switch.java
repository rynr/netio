package org.rjung.netio.api.enums;

public enum Switch {
    OFF("0"),
    ON("1"),
    MANUAL("manual"),
    INTERRUPT("int");

    private String command;

    Switch(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
