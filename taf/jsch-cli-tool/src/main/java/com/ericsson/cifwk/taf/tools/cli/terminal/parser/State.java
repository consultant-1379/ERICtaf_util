package com.ericsson.cifwk.taf.tools.cli.terminal.parser;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

public class State {

    private static final boolean ACTION_PERFORMED = true;
    private static final boolean ACTION_NOT_PERFORMED = false;

    protected String name;
    protected List<Action> entryActions = new ArrayList<>();
    protected List<Event> events = new ArrayList<>();
    protected List<Action> exitActions = new ArrayList<>();

    public State(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    State onEntry(Action action) {
        entryActions.add(action);
        return this;
    }

    State onExit(Action action) {
        exitActions.add(action);
        return this;
    }

    State event(Matcher matcher, Action... actions) {
        events.add(new Event(matcher, actions));
        return this;
    }

    void entry() {
        for (Action action : entryActions) action.perform();
    }

    void exit() {
        for (Action action : exitActions) action.perform();
    }

    boolean process(char c) {
        for (Event event : events) {
            if (event.matches(c)) {
                event.perform();
                return ACTION_PERFORMED;
            }
        }
        return ACTION_NOT_PERFORMED;
    }

}
