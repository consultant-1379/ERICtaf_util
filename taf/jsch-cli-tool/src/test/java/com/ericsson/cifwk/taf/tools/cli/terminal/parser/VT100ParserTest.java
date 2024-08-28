package com.ericsson.cifwk.taf.tools.cli.terminal.parser;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * see: <a href="http://en.wikipedia.org/wiki/C0_and_C1_control_codes">C0 and C1 control codes</a>
 * see: <a href="http://en.wikipedia.org/wiki/ANSI_escape_code">ANSI escape code</a>
 */
public class VT100ParserTest {

    VT100Parser parser = VT100Parser.parser();

    @Test
    public void shouldPrintCRLF() throws Exception {
        String out = parser.parse("A\n\rB");
        assertThat(out, equalTo("A\n\rB"));
    }


    @Test
    public void shouldPrintAllChars_from_0x20_to_0x7F() throws Exception {
        String in = string(0x20, 0x7F);
        String out = parser.parse(in);
        assertThat(out, equalTo(in));
    }

    @Test
    public void shouldOnlyCRLF_fromC0_codes() throws Exception {
        String in = "A" + string(0x00, 0x1A) + string(0x1C, 0x1F) + "B";
        String out = parser.parse(in);
        assertThat(out, equalTo("A\n\rB"));
    }

    public static final String ANSI_PM = "\u001B^";     // C1 Privacy Message
    public static final String ANSI_APC = "\u001B_";    // C1 Application Program Command
    public static final String ANSI_SOS = "\u001BX";    // C1 Start of String
    public static final String ANSI_ST = "\u001B\\";    // C1 String Terminator

    @Test
    public void shouldIgore_PM_APC_SOS_codes_sequance() throws Exception {
        String in = "A" +
                ANSI_PM + "12345678" + ANSI_ST +
                ANSI_APC + "12345678" + ANSI_ST +
                ANSI_SOS + "12345678" + ANSI_ST +
                "B";
        String out = parser.parse(in);
        assertThat(out, equalTo("AB"));
    }

    public static final String ANSI_OSC = "\u001B]";    // C1 Operating system command

    /**
     * see: <a href="http://en.wikipedia.org/wiki/C0_and_C1_control_codes">C0 and C1 control codes</a>
     *
     * @throws Exception
     */
    @Test
    public void shouldIgore_OSC_codes_sequance_till_ESCAPE_STRING_TERMINATOR() throws Exception {
        String in = "A" +
                ANSI_OSC + "12345678" + ANSI_ST +
                "B";
        String out = parser.parse(in);
        assertThat(out, equalTo("AB"));
    }

    public static final String ANSI_DCS = "\u001B]"; // C1 Device control string

    /**
     * see: <a href="http://en.wikipedia.org/wiki/C0_and_C1_control_codes">C0 and C1 control codes</a>
     *
     * @throws Exception
     */
    @Test
    public void shouldIgore_DCS_codes_sequance_till_ESCAPE_STRING_TERMINATOR() throws Exception {
        String in = "A" +
                ANSI_DCS + "12345678" + ANSI_ST +
                "B";
        String out = parser.parse(in);
        assertThat(out, equalTo("AB"));
    }


    @Test
    public void shouldIgnoreForegroundColoring_CSI() throws Exception {
        // <ESC>[30m - <ESC>[39m
        for (int i = 0; i < 10; i++) {
            String in = String.format("A\u001B[3%1$1dmB", i);
            String out = parser.parse(in);
            assertThat(out, equalTo("AB"));

        }
    }

    @Test
    public void shouldIgnoreBackgroundColoring_CSI() throws Exception {
        // <ESC>[40m - <ESC>[49m
        for (int i = 0; i < 10; i++) {
            String in = String.format("A\u001B[4%1$1dmB", i);
            String out = parser.parse(in);
            assertThat(out, equalTo("AB"));

        }
    }


    private String string(int i1, int i2) {
        char[] chars = new char[i2 - i1 + 1];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (i1 + i);
        }
        return String.copyValueOf(chars);
    }


}
