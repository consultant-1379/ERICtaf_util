package com.ericsson.cifwk.taf.tools.cli.terminal.parser;

public class VT100Parser extends TerminalParser<VT100ParserState> {

    public static VT100Parser parser() {
        return parser(new VT100ParserAction());
    }

    protected VT100Parser() {
    }

    static VT100Parser parser(VT100ParserAction action) {
        VT100Parser parser = new VT100Parser();
        action.parser = parser;
        VT100ParserState state = new VT100ParserState(action);
        parser.init(state);
        return parser;
    }

    State ANYWHERE;

    protected void init(VT100ParserState state) {
        super.init(state);
        ANYWHERE = state.ANYWHERE;

    }

    @Override
    protected void process(char c) {
        processed = c;
        if (!ANYWHERE.process(c)) state.process(c);
    }

}