package com.ericsson.cifwk.taf.tools.cli.terminal.parser;

public class VT100ParserAction extends TerminalParserAction<VT100Parser> {

    public Action collect() {
        return Action.DO_NOTHING;
    }

    public Action esc_dispatch() {
        return Action.DO_NOTHING;
    }

    public Action csi_dispatch() {
        return Action.DO_NOTHING;
    }

    public Action param() {
        return Action.DO_NOTHING;
    }

    public Action clear() {
        return Action.DO_NOTHING;
    }

    public Action hook() {
        return Action.DO_NOTHING;
    }

    public Action unhook() {
        return Action.DO_NOTHING;
    }

    public Action put() {
        return Action.DO_NOTHING;
    }

    public Action osc_start() {
        return Action.DO_NOTHING;
    }

    public Action osc_end() {
        return Action.DO_NOTHING;
    }

    public Action osc_put() {
        return Action.DO_NOTHING;
    }
}
