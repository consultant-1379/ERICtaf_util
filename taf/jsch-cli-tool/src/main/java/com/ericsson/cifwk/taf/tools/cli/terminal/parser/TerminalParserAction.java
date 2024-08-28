package com.ericsson.cifwk.taf.tools.cli.terminal.parser;

public class TerminalParserAction<T extends TerminalParser> {

    protected T parser;

    protected void setParser(T parser) {
        this.parser = parser;
    }

    /**
     * The character or control is not processed.
     *
     * @return
     */
    protected Action ignore() {
        return Action.DO_NOTHING;
    }

    /**
     * The current code should be mapped to a glyph, and that glyph should be displayed.
     *
     * @return
     */
    protected Action print() {
        return new Action() {
            public void perform() {
                parser.result.append(parser.processed);
            }
        };
    }

    public Action execute() {
        return new Action() {
            @Override
            public void perform() {
                if (parser.processed == '\r') print().perform();
                if (parser.processed == '\n') print().perform();
            }
        };
    }

    protected Action transitionTo(final State state) {
        return new Action() {
            public void perform() {
                parser.state.exit();
                parser.state = state;
                parser.state.entry();
            }
        };
    }

}
