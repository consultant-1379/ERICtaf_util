package com.ericsson.cifwk.taf.tools.cli.terminal.parser;

public abstract class Action {
    public static final Action DO_NOTHING = new Action(){
        public void perform() {
            // do nothing
        }
    };
    public abstract void perform();
}
