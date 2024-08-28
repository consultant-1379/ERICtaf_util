package com.ericsson.cifwk.taf.handlers.netsim;

import java.util.Map;
import java.util.List;
import com.ericsson.cifwk.taf.handlers.netsim.lang.NetSimConstants;
import com.google.common.base.Joiner;

class CommandLineBuilderImpl implements CommandLineBuilder {

    private static final String CMD_START = "echo -e '";

    private static final String CMD_END = "' | " + ShellCommands.NETSIM_PIPE_CMD;

    private final StringBuilder commandLine;

    private Style style;

    private String delimiter = "";

    private enum Style {
        COMMON,
        MML,
        ERLANG
    }

    public CommandLineBuilderImpl() {
        this.commandLine = new StringBuilder();
    }

    @Override
    public void start() {
        commandLine.append(CMD_START).append(" ");
    }

    @Override
    public void end() {
        commandLine.append(" ").append(CMD_END);
    }

    @Override
    public void startCommand(String command) {

        if (command.startsWith(".")) {
            this.style = Style.COMMON;
        } else if (command.startsWith("e ")){
            this.style = Style.ERLANG;
        } else {
            this.style = Style.MML;
        }
        commandLine.append(command);
    }

    @Override
    public void endCommand() {
        if (Style.MML.equals(style)) {
            commandLine.append(";").append(NetSimConstants.NEW_LINE);
        } else if (Style.ERLANG.equals(style)) {
            commandLine.append(".").append(NetSimConstants.NEW_LINE);
        } else {
            commandLine.append(NetSimConstants.NEW_LINE);
        }
    }

    @Override
    public void beforeParameters(Map<NetSimCommandEmitter.AttributeKey, Object> attributeMap) {
        if (Style.MML.equals(style) || Style.ERLANG.equals(style)) {
            if (!attributeMap.isEmpty()) {
                commandLine.append(":");
            }
        } else {
            commandLine.append(" ");
        }
        this.delimiter = "";
    }

    @Override
    public void addParameter(NetSimCommandEmitter.AttributeKey key, Object value, boolean hasNext) {
        String stringValue = convertToString(key, value);
        Class<?> type = key.getType();
        if (stringValue == null) {
            return;
        }

        commandLine.append(this.delimiter);

        String delimiter;
        if (Style.MML.equals(style)) {
            delimiter = ",";
            if (Boolean.TYPE.equals(type)) {
                commandLine.append(key.getName());
            } else if(!key.requiresAssignment()){
                commandLine.append(value);
            } else {
                commandLine.append(key.getName()).append("=").append(stringValue);
            }
        } else if(Style.ERLANG.equals(style)){
            delimiter = "";
            commandLine.append(key.getName()).append(stringValue);

        } else {
            delimiter = " ";
            commandLine.append(stringValue);
        }
        if (hasNext) {
            this.delimiter = delimiter;
        }
    }

    private String convertToString(NetSimCommandEmitter.AttributeKey key, Object value) {
        Class<?> type = key.getType();
        if (Boolean.TYPE.equals(type)) {
            if ((boolean) value) {
                return key.getName();
            }
            return null;
        } else if (String[].class.equals(type)) {
            String joined = Joiner.on(", ").join((Object[]) value);
            if (Style.MML.equals(style)) {
                return quoted(joined);
            } else {
                return joined;
            }
        } else if (List.class.equals(type)) {
            String expression = ErlangExpression.build((List<?>) value);
            return quoted(expression);
        } else if (String.class.equals(type)) {
            if (Style.MML.equals(style) && key.isQuoted()) {
                return quoted(value.toString());
            } else if (Style.ERLANG.equals(style)) {
            return value.toString().replaceAll("'", "'\\\\''");
        }else {
                return value.toString();
            }
        } else {
            if (key.isQuoted()) {
                return quoted(value.toString());
            } else {
                return value.toString();
            }
        }
    }

    private String quoted(String value) {
        return "\"" + value + "\"";
    }

    @Override
    public String build() {
        return commandLine.toString();
    }

}
