package com.ericsson.cifwk.taf.tools.cli.terminal.parser;

import org.hamcrest.Matcher;

import static com.ericsson.cifwk.taf.tools.cli.terminal.parser.MatcherHelper.anyOf;
import static com.ericsson.cifwk.taf.tools.cli.terminal.parser.MatcherHelper.between;
import static com.ericsson.cifwk.taf.tools.cli.terminal.parser.MatcherHelper.equalTo;

public class TerminalParserState<T extends TerminalParserAction> extends State {

    protected T action;

    protected final State GROUND;

    static protected final Matcher IS_CONTROL_CHARACTERS = anyOf(between(0x00, 0x17), equalTo(0x19), between(0x1C, 0x1F));


    public TerminalParserState(T action) {
        super("GROUND");
        this.action = action;
        GROUND = this
                .event(IS_CONTROL_CHARACTERS, action.execute())
                .event(between(0x20, 0x7F), action.print())
                .event(between(0xA0, 0xFF), action.print());
    }

}
