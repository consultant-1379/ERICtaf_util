package com.ericsson.cifwk.taf.handlers.netsim;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 *
 */
public class NetSimCommandEmitterTest {

    @Test
    public void shouldGenerateCommonCommand() {
        SampleCommand command = new SampleCommand();
        command.array = new String[] { "a", "b", "c" };
        command.flag = true;
        command.string = "abc";

        String line = NetSimCommandEmitter.buildCommandLine(command);

        assertThat(line, equalTo("echo -e ' .test netsim abc -flag_param a, b, c\n ' | /netsim/inst/netsim_pipe"));
    }

    @Test
    public void shouldGenerateCommandWithoutQuotes_ForWithquotedAttrValFalse() {
        QuoteTestCommand command = new QuoteTestCommand();
        command.setIdWithquotedAttrValFalse("1");

        String line = NetSimCommandEmitter.buildCommandLine(command);

        assertThat(line, equalTo("echo -e ' quotetest:idWithquotedAttrValFalse=1;\n ' | /netsim/inst/netsim_pipe"));
    }

    @Test
    public void shouldGenerateCommandWithQuotes_ForWithquotedAttrValTrue() {
        QuoteTestCommand command = new QuoteTestCommand();
        command.setIdWithquotedAttrValTrue("1");

        String line = NetSimCommandEmitter.buildCommandLine(command);

        assertThat(line, equalTo("echo -e ' quotetest:idWithquotedAttrValTrue=\"1\";\n ' | /netsim/inst/netsim_pipe"));
    }

    @Test
    public void shouldGenerateCommandWithQuotes_ForWithoutquotedAttr() {
        QuoteTestCommand command = new QuoteTestCommand();
        command.setIdWithoutquotedAttr("1");

        String line = NetSimCommandEmitter.buildCommandLine(command);

        assertThat(line, equalTo("echo -e ' quotetest:idWithoutquotedAttr=\"1\";\n ' | /netsim/inst/netsim_pipe"));
    }

    @Test
    public void shouldGenerateCommandWithoutTrailingCommas() {
        MMLCommand command = new MMLCommand();
        command.setScope(8);
        command.setMoid("1");

        String line = NetSimCommandEmitter.buildCommandLine(command);

        assertThat(line, equalTo("echo -e ' dumpmotree:moid=\"1\",scope=8;\n ' | /netsim/inst/netsim_pipe"));
    }

    @Test
    public void shouldGenerateCommonCommand_Empty() {
        SampleCommand command = new SampleCommand();
        String line = NetSimCommandEmitter.buildCommandLine(command);
        assertThat(line, equalTo("echo -e ' .test netsim \n ' | /netsim/inst/netsim_pipe"));
    }

    @Test
    public void shouldGenerateSpecificCommand() {
        ExtraCommand command = new ExtraCommand();
        command.question = Boolean.TRUE;
        command.ratio = new BigDecimal("1.33");
        command.a = 1L;
        command.b = 2;
        command.stuff = ExtraCommand.Stuff.ONE;

        String line = NetSimCommandEmitter.buildCommandLine(command);

        assertThat(line, equalTo("echo -e ' hello:question=true,ratio=1.33,a=1,b=2,stuff=x;\n ' | /netsim/inst/netsim_pipe"));
    }

    @Test
    public void shouldGenerateSpecificCommand_Empty() {
        ExtraCommand command = new ExtraCommand();

        String line = NetSimCommandEmitter.buildCommandLine(command);

        assertThat(line, equalTo("echo -e ' hello;\n ' | /netsim/inst/netsim_pipe"));
    }

    @Test
    public void shouldGenerateSpecificCommand_Struct() {
        ExtraCommand command = new ExtraCommand();
        command.struct = new ArrayList<>();

        HashMap<String, List> first = new HashMap<>();
        ArrayList<Map<String, String>> firstList = new ArrayList<>();
        firstList.add(Collections.singletonMap("userLabel", "kalle"));
        first.put("managedElement=1", firstList);
        HashMap<String, Object> second = new HashMap<>();
        ArrayList<Map<String, String>> secondList = new ArrayList<>();
        secondList.add(Collections.singletonMap("userLabel", "olle"));
        second.put("managedElement=2", secondList);

        command.struct.add(first);
        command.struct.add(second);

        String line = NetSimCommandEmitter.buildCommandLine(command);

        assertThat(line, equalTo("echo -e ' hello:struct=" + "\"[{\\\"managedElement=1\\\",[{\\\"userLabel\\\",\\\"kalle\\\"}]},"
                + "{\\\"managedElement=2\\\",[{\\\"userLabel\\\",\\\"olle\\\"}]}]\"" + ";\n ' | /netsim/inst/netsim_pipe"));
    }

    @Test
    public void shouldWorkOnLists() {
        ListCommand command = new ListCommand();
        command.array = new String[] { "x", "y" };

        String line = NetSimCommandEmitter.buildCommandLine(command);

        assertThat(line, equalTo("echo -e ' list:arr=\"x, y\";\n ' | /netsim/inst/netsim_pipe"));
    }

    @Test
    public void shouldGenerateNetSimShellCommand() {
        SampleCommand command = new SampleCommand();
        List<NetSimCommand> cmdList = new ArrayList<>();
        cmdList.add(command);
        String line = NetSimCommandEmitter.buildShellCommand(cmdList);
        assertThat(line, equalTo(".test netsim \n"));
    }

    @Test
    public void shouldGetCmdExecutionStartPattern() {
        assertThat(NetSimCommandEmitter.getCmdExecutionStartPattern(SampleCommand.class), equalTo("(?s).*>>.*.test netsim.*"));
        assertThat(NetSimCommandEmitter.getCmdExecutionStartPattern(SampleCommand.class, "param1", "param2"), equalTo("(?s).*>>.*.test netsim param1 param2.*"));
    }

    @Test
    public void shoulgenerateNetsimErlangCommand() {
        ErlangCommand command = new ErlangCommand();
        command.setScaler("('ENTITY-MIB', entLastChangeTime, 162950700)");
        String line = NetSimCommandEmitter.buildCommandLine(command);
        assertThat(line, equalTo("echo -e ' e netsim_snmp_oiddb:set_scalar('\\''ENTITY-MIB'\\'', entLastChangeTime, 162950700).\n ' | "
                + "/netsim/inst/netsim_pipe"));

    }

    @Cmd(value = "list")
    public static class ListCommand implements NetSimCommand {
        String[] array;

        @Cmd(value = "arr", index = 0)
        public String[] getArray() {
            return this.array;
        }
    }

    @Cmd(value = "e netsim_snmp_oiddb")
    public static class ErlangCommand implements NetSimCommand {
        String scaler;

        @Cmd(value = "set_scalar", index = 0)
        public String getSet_scaler() {
            return this.scaler;
        }

        public ErlangCommand setScaler(String value) {
            this.scaler = value;
            return this;
        }
    }

    @Cmd(value = "hello")
    public static class ExtraCommand implements NetSimCommand {
        public enum Stuff {
            ONE("x"),
            TWO("y");

            private final String value;

            Stuff(String value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return value;
            }
        }

        Boolean question;

        BigDecimal ratio;

        Long a;

        Integer b;

        Stuff stuff;

        List<Map> struct;

        @Cmd(value = "question", index = 0, quoted = false)
        public Boolean getQuestion() {
            return question;
        }

        @Cmd(value = "ratio", index = 1, quoted = false)
        public BigDecimal getRatio() {
            return ratio;
        }

        @Cmd(value = "a", index = 2, quoted = false)
        public Long getA() {
            return a;
        }

        @Cmd(value = "b", index = 3, quoted = false)
        public Integer getB() {
            return b;
        }

        @Cmd(value = "stuff", index = 4, quoted = false)
        public Stuff getStuff() {
            return stuff;
        }

        @Cmd(value = "struct", index = 5, quoted = false)
        public List<Map> getStruct() {
            return struct;
        }
    }

    @Cmd(value = ".test netsim")
    public static class SampleCommand implements NetSimCommand {

        String string;

        boolean flag;

        String[] array;

        @Cmd(value = "string_param", index = 0)
        public String getString() {
            return string;
        }

        @Cmd(value = "-flag_param", index = 1)
        public boolean isFlag() {
            return flag;
        }

        @Cmd(value = "array_param", index = 2)
        public String[] getArray() {
            return array;
        }

    }

    @Cmd(value = "dumpmotree")
    public static class MMLCommand implements NetSimCommand {

        private String moid;

        private Integer scope;

        private boolean printattrs = false;

        private boolean pmValue = false;

        @Cmd(value = "moid", index = 0)
        public String getMoid() {
            return this.moid;
        }

        public MMLCommand setMoid(String value) {
            this.moid = value;
            return this;
        }

        @Cmd(value = "scope", index = 1, quoted = false)
        public Integer getScope() {
            return this.scope;
        }

        public MMLCommand setScope(Integer value) {
            this.scope = value;
            return this;
        }

        @Cmd(value = "printattrs", index = 2)
        public boolean getPrintattrs() {
            return this.printattrs;
        }

        public MMLCommand setPrintattrs(boolean value) {
            this.printattrs = value;
            return this;
        }

        @Cmd(value = "pm_value", index = 3)
        public boolean getPmValue() {
            return this.pmValue;
        }

        public MMLCommand setPmValue(boolean value) {
            this.pmValue = value;
            return this;
        }

    }

    @Cmd(value = "quotetest")
    public final class QuoteTestCommand implements NetSimCommand {

        private String idWithquotedAttrValFalse;

        private String idWithquotedAttrValTrue;

        private String idWithoutquotedAttr;

        QuoteTestCommand() {
        }

        @Cmd(value = "idWithquotedAttrValFalse", index = 0, quoted = false)
        public String getIdWithquotedAttrValFalse() {
            return this.idWithquotedAttrValFalse;
        }

        public QuoteTestCommand setIdWithquotedAttrValFalse(String value) {
            this.idWithquotedAttrValFalse = value;
            return this;
        }

        @Cmd(value = "idWithquotedAttrValTrue", index = 1, quoted = true)
        public String getIdWithquotedAttrValTrue() {
            return this.idWithquotedAttrValTrue;
        }

        public QuoteTestCommand setIdWithquotedAttrValTrue(String value) {
            this.idWithquotedAttrValTrue = value;
            return this;
        }

        @Cmd(value = "idWithoutquotedAttr", index = 2)
        public String getIdWithoutquotedAttr() {
            return this.idWithoutquotedAttr;
        }

        public QuoteTestCommand setIdWithoutquotedAttr(String value) {
            this.idWithoutquotedAttr = value;
            return this;
        }

    }

}
