package com.ericsson.cifwk.taf.tools.cli.terminal.parser;

import org.hamcrest.Matcher;

public class Event {
    Matcher matcher;
    Action[] actions;

    public Event(Matcher matcher, Action... action) {
        this.matcher = matcher;
        this.actions = action;
    }

    public void perform() {
        for (Action action : actions) action.perform();
    }

    public boolean matches(char c) {
        return matcher.matches(c);
    }
}
