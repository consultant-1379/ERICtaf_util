package com.ericsson.cifwk.taf.tools.cli.terminal.parser;

import java.nio.CharBuffer;

public class TerminalParser<T extends State> {

    protected TerminalParser() {
    }

    public static TerminalParser parser() {
        TerminalParser parser = new TerminalParser();
        TerminalParserAction action = new TerminalParserAction();
        action.parser = parser;
        State state = new TerminalParserState(action);
        parser.init(state);
        return parser;
    }

    protected void init(T state) {
        this.state = state;
    }

    protected State state;
    protected char processed;
    protected CharBuffer result = CharBuffer.allocate(1024);

    public String parse(String string) {
        for (char c : string.toCharArray()) {
            process(c);
        }

        char[] parse = new char[result.position()];
        result.rewind();
        result.get(parse);
        result.rewind();
        return String.copyValueOf(parse);
    }

    protected void process(char c) {
        processed = c;
        state.process(c);
    }

}
