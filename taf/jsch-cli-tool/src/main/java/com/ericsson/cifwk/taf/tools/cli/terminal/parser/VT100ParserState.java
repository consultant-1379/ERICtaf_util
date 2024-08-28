package com.ericsson.cifwk.taf.tools.cli.terminal.parser;

import org.hamcrest.Matcher;

import static com.ericsson.cifwk.taf.tools.cli.terminal.parser.MatcherHelper.anyOf;
import static com.ericsson.cifwk.taf.tools.cli.terminal.parser.MatcherHelper.between;
import static com.ericsson.cifwk.taf.tools.cli.terminal.parser.MatcherHelper.equalTo;

class VT100ParserState extends TerminalParserState<VT100ParserAction> {

	static protected final Matcher IS_DEL = equalTo(0x7F);
	static protected final Matcher IS_ESCAPE = equalTo(0x1B);

	protected final State CSI_IGNORE;
	protected final State CSI_INTERMEDIATE;
	protected final State CSI_PARAM;
	protected final State CSI_ENTRY;
	protected final State ESCAPE_INTERMEDIATE;
	protected final State ESCAPE;
	protected final State PM_APC_SOS_STRING;
	protected final State DCS_ENTRY;
	protected final State DCS_INTERMEDIATE;
	protected final State DCS_IGNORE;
	protected final State DCS_PARAM;
	protected final State DCS_PASSTHROUGH;
	protected final State OSC_STRING;
	protected final State ANYWHERE;


	public VT100ParserState(VT100ParserAction action) {
		super(action);

		CSI_IGNORE = new State("CSI_IGNORE")
		.event(IS_CONTROL_CHARACTERS, action.execute())
		.event(IS_DEL, action.ignore())
		.event(between(0x40, 0x7E), action.transitionTo(GROUND));

		CSI_INTERMEDIATE = new State("CSI_INTERMEDIATE")
		.event(IS_CONTROL_CHARACTERS, action.execute())
		.event(IS_DEL, action.ignore())
		.event(between(0x20, 0x2F), action.collect())
		.event(between(0x40, 0x7E), action.csi_dispatch(), action.transitionTo(GROUND))
		.event(between(0x30, 0x3F), action.transitionTo(CSI_IGNORE));

		CSI_PARAM = new State("CSI_PARAM")
		.event(IS_CONTROL_CHARACTERS, action.execute())
		.event(IS_DEL, action.ignore())
		.event(anyOf(between(0x30, 0x39), equalTo(0x3B)), action.param())
		.event(between(0x40, 0x7E), action.csi_dispatch(), action.transitionTo(GROUND))
		.event(anyOf(equalTo(0x3A), between(0x3C, 0x3F)), action.transitionTo(CSI_IGNORE))
		.event(between(0x20, 0x2F), action.collect(), action.transitionTo(CSI_INTERMEDIATE));

		CSI_ENTRY = new State("CSI_ENTRY")
		.onEntry(action.clear())
		.event(IS_CONTROL_CHARACTERS, action.execute())
		.event(IS_DEL, action.ignore())
		.event(between(0x40, 0x7F), action.csi_dispatch(), action.transitionTo(GROUND))
		.event(anyOf(between(0x30, 0x39), equalTo(0x3B)), action.param(), action.transitionTo(CSI_PARAM))
		.event(between(0x3C, 0x3F), action.collect(), action.transitionTo(CSI_PARAM))
		.event(equalTo(0x3A), action.transitionTo(CSI_IGNORE))
		.event(between(0x20, 0x2F), action.collect(), action.transitionTo(CSI_INTERMEDIATE));

		ESCAPE_INTERMEDIATE = new State("ESCAPE_INTERMEDIATE")
		.event(IS_CONTROL_CHARACTERS, action.execute())
		.event(IS_DEL, action.ignore())
		.event(between(0x20, 0x2F), action.collect())
		.event(between(0x30, 0x7E), action.esc_dispatch(), action.transitionTo(GROUND));

		PM_APC_SOS_STRING = new State("PM_APC_SOS_STRING")
		.event(IS_CONTROL_CHARACTERS, action.ignore())
		.event(IS_DEL, action.ignore())
		.event(between(0x20, 0x7F), action.ignore());


		DCS_PASSTHROUGH = new State("DCS_PASSTHROUGH")
		.onEntry(action.hook())
		.event(IS_CONTROL_CHARACTERS, action.put())
		.event(IS_DEL, action.ignore())
		.event(between(0x20, 0x7F), action.put())
		.onExit(action.unhook());

		DCS_IGNORE = new State("DCS_IGNORE")
		.event(IS_CONTROL_CHARACTERS, action.ignore());

		DCS_INTERMEDIATE = new State("DCS_INTERMEDIATE")
		.event(IS_CONTROL_CHARACTERS, action.ignore())
		.event(IS_DEL, action.ignore())
		.event(between(0x20, 0x2F), action.collect())
		.event(between(0x30, 0x3F), action.transitionTo(DCS_IGNORE))
		.event(between(0x40, 0x37), action.transitionTo(DCS_PASSTHROUGH));

		DCS_PARAM = new State("DCS_PARAM")
		.event(IS_CONTROL_CHARACTERS, action.ignore())
		.event(IS_DEL, action.ignore())
		.event(anyOf(between(0x30, 0x39), equalTo(0x3B)), action.param())
		.event(anyOf(equalTo(0x3A), between(0x30, 0x3F)), action.transitionTo(DCS_IGNORE))
		.event(between(0x20, 0x2F), action.collect(), action.transitionTo(DCS_INTERMEDIATE));

		DCS_ENTRY = new State("DCS_ENTRY")
		.onEntry(action.clear())
		.event(IS_CONTROL_CHARACTERS, action.ignore())
		.event(IS_DEL, action.ignore())
		.event(equalTo(0x3A), action.transitionTo(DCS_IGNORE))
		.event(between(0x20, 0x2F), action.collect(), action.transitionTo(DCS_INTERMEDIATE))
		.event(anyOf(between(0x30, 0x39), equalTo(0x3B)), action.param(), action.transitionTo(DCS_PARAM))
		.event(between(0x3C, 0x3F), action.collect(), action.transitionTo(DCS_PARAM));

		OSC_STRING = new State("OSC_STRING")
		.onEntry(action.osc_start())
		.event(equalTo(0x3b), action.transitionTo(GROUND))
		.event(IS_CONTROL_CHARACTERS, action.ignore())
		.event(between(0x20, 0x7F), action.osc_put())                
		.onExit(action.osc_end());

		ESCAPE = new State("ESCAPE")
		.onEntry(action.clear())
		.event(IS_CONTROL_CHARACTERS, action.execute())
		.event(IS_DEL, action.ignore())
		.event(anyOf(
				between(0x30, 0x4F), between(0x51, 0x57), equalTo(0x59), equalTo(0x5A), equalTo(0x5C), between(0x60, 0x7E)),
				action.esc_dispatch(), action.transitionTo(GROUND))
				.event(equalTo(0x5B), action.transitionTo(CSI_ENTRY))
				.event(between(0x20, 0x2F), action.collect(), action.transitionTo(ESCAPE_INTERMEDIATE))
				.event(anyOf(equalTo(0x58), equalTo(0x5E), equalTo(0x5F)), action.transitionTo(PM_APC_SOS_STRING))
				.event(equalTo(0x50), action.transitionTo(DCS_ENTRY))
				.event(equalTo(0x5D), action.transitionTo(OSC_STRING));

		ANYWHERE = new State("ANYWHERE")
		.event(IS_ESCAPE, action.transitionTo(ESCAPE))
		.event(equalTo(0x90), action.transitionTo(DCS_ENTRY))
		.event(equalTo(0x9B), action.transitionTo(CSI_ENTRY))
		.event(equalTo(0x9D), action.transitionTo(OSC_STRING))
		.event(anyOf(equalTo(0x98), equalTo(0x9E), equalTo(0x9F)), action.transitionTo(PM_APC_SOS_STRING))
		.event(equalTo(0x9C), action.transitionTo(GROUND))
		.event(anyOf(equalTo(0x18), equalTo(0x1A)), action.execute(), action.transitionTo(GROUND))
		.event(anyOf(between(0x80, 0x8F), between(0x91, 0x97), equalTo(0x99), equalTo(0x9A)), action.execute(), action.transitionTo(GROUND));

	}

}
