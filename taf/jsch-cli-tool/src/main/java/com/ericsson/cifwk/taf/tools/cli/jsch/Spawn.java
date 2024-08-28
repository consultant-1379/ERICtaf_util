package com.ericsson.cifwk.taf.tools.cli.jsch;

import com.ericsson.cifwk.taf.configuration.TafDataHandler;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.ericsson.cifwk.taf.tools.cli.terminal.parser.TerminalParser;
import com.ericsson.cifwk.taf.tools.cli.terminal.parser.VT100Parser;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import expectj.ExpectJ;
import expectj.ExpectJException;
import expectj.Spawnable;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Spawn {

    /**
     * This pattern allows to: set PROMPT pattern via configuration flags match
     * prompt when multiple lines are returned
     */
    private static final String PROMPT_PATTERN = TafDataHandler.getAttribute("cli.prompt.pattern", "(?s).*[#>$]\\s*");

    /**
     * Warning!!! Please didn't change this parameter ExpectJ v 2.0.7 has
     * bug(feature), the spawn be alive only this default time and will be
     * closed after it's see {@link expectj.SpawnableHelper} timer field
     */
    private static final int EXPECTJ_DEFAULT_TIMEOUT_SECONDS = -1;

    private static final Logger logger = LoggerFactory.getLogger(Spawn.class);

    private static final String JSCH_EXIT_CODE_MARKER = "JSCH:EXIT_CODE";

    private static final String JSCH_EXIT_CODE_0 = JSCH_EXIT_CODE_MARKER + "=0";

    private static final String ECHO_EXIT_CODE_CMD = "echo \"" + JSCH_EXIT_CODE_MARKER + "=$?\"";

    /**
     * Property to set maximum timeout for creating the Shell; Default 60
     * seconds
     */
    public static final String SPAWN_CREATION_TIMEOUT_PROPERTY = "taf.shell.creation.timeout";

    private static final int SPAWN_CREATION_TIMEOUT_SECONDS = TafDataHandler.getAttribute(SPAWN_CREATION_TIMEOUT_PROPERTY, 60);

    private expectj.Spawn spawn;

    private SpawnHelper spawnHelper;

    private Channel channel;

    private long timeOutSeconds = -1;

    private Map<String, String> envs = new HashMap<>();

    private Map<String, String> defaultEnvs = new HashMap<>();

    static Spawn shell(ChannelShell channel, int defaultTimeOutSeconds) {
        try {
            VT100Parser parser = VT100Parser.parser();
            Spawn spawn = spawn(channel, defaultTimeOutSeconds, parser);
            final long oldTimeout = spawn.timeOutSeconds;
            spawn.timeOutSeconds = SPAWN_CREATION_TIMEOUT_SECONDS;
            spawn.defaultEnvs = spawn.getEnv();
            spawn.timeOutSeconds = oldTimeout;
            return spawn;
        } catch (IOException | TimeoutException e) {
            throw new JSchCLIToolException(e);
        }
    }

    static Spawn exec(ChannelExec channel, int defaultTimeOutSeconds) {
        return spawn(channel, defaultTimeOutSeconds, null);
    }

    static Spawn spawn(Channel channel, int defaultTimeOutSeconds, @SuppressWarnings("rawtypes") TerminalParser parser) {
        try {
            Spawn spawn = new Spawn();
            spawn.channel = channel;
            Spawnable spawnable = new SshSpawn(channel);
            spawn.spawn = new ExpectJ(EXPECTJ_DEFAULT_TIMEOUT_SECONDS).spawn(spawnable);
            spawn.spawnHelper = new SpawnHelper(spawn.spawn, parser).init();
            spawn.timeOutSeconds = defaultTimeOutSeconds;
            return spawn;
        } catch (IOException e) {
            throw new JSchCLIToolException(e);
        }
    }

    static Spawn telnetSpawn(String host, int port, int defaultTimeOutSeconds) {
        try {
            VT100Parser parser = VT100Parser.parser();
            Spawn spawn = new Spawn();
            spawn.spawn = new ExpectJ(EXPECTJ_DEFAULT_TIMEOUT_SECONDS).spawn(host, port);
            spawn.spawnHelper = new SpawnHelper(spawn.spawn, parser);
            spawn.timeOutSeconds = defaultTimeOutSeconds;
            return spawn;
        } catch (IOException e) {
            throw new JSchCLIToolException(e);
        }
    }

    public void send(String string) throws IOException {
        logger.debug("Sending '{}'",string);
        spawn.send(string);
    }

    public String expect(String subString) throws IOException {
        return spawnHelper.expect(subString, this.timeOutSeconds);
    }

    public String expect(String subString, long timeOutSeconds) throws IOException {
        return spawnHelper.expect(subString, timeOutSeconds);
    }

    public String expect(Pattern pattern) throws IOException {
        return spawnHelper.expect(pattern, this.timeOutSeconds);
    }

    public String expect(Pattern pattern, long timeOutSeconds) throws IOException {
        return spawnHelper.expect(pattern, timeOutSeconds);
    }

    public void expectErr(String subString) throws IOException {
        try {
            spawn.expectErr(subString, this.timeOutSeconds);
        } catch (expectj.TimeoutException e) {
            throw new TimeoutException("Timeout trying to match \"" + subString + "\"");
        }
    }

    public void expectErr(String subString, long timeOutSeconds) throws IOException {
        try {
            spawn.expectErr(subString, timeOutSeconds);
        } catch (expectj.TimeoutException e) {
            throw new TimeoutException("Timeout trying to match \"" + subString + "\"");
        }
    }

    public void stop() {
        spawn.stop();
    }

    public boolean isClosed() {
        return spawn.isClosed();
    }

    public void expectClose() throws ExpectJException {
        try {
            spawn.expectClose(this.timeOutSeconds);
        } catch (expectj.TimeoutException e) {
            throw new TimeoutException("Timeout waiting session close");
        }
    }

    public void expectClose(long timeOutSeconds) throws ExpectJException {
        try {
            spawn.expectClose(timeOutSeconds);
        } catch (expectj.TimeoutException e) {
            throw new TimeoutException("Timeout waiting session close");
        }
    }

    public int getExitValue() throws ExpectJException {
        return getExitValue(this.timeOutSeconds);
    }

    public int getExitValue(long timeOutSeconds) throws ExpectJException {
        if (!isClosed()) {
            expectClose(timeOutSeconds);
        }
        return channel.getExitStatus();
    }

    public String sendCommand(String... commands) throws IOException, TimeoutException {
        StringBuilder send = new StringBuilder();
        for (String command : commands) {
            send.append(command).append(";");
        }
        send.append(ECHO_EXIT_CODE_CMD).append("\n");
        spawn.send(send.toString());
        return expect(JSCH_EXIT_CODE_0, timeOutSeconds);
    }

    public Map<String, String> getEnv() throws IOException {
        // Send env command only if the prompt appears
        String expectOutput = null;
        try {
            expectOutput = expect(Pattern.compile(PROMPT_PATTERN), timeOutSeconds);
        } catch (TimeoutException e) {
            getLogger().warn("Expected Prompt not received within {} seconds got {} instead", timeOutSeconds, expectOutput);
        }
        String stdout = "";
        try{
            stdout = sendCommand("env");
        } catch (TimeoutException e) {
            getLogger().warn("Can't retrieve Environment Variables of current shell ", e);
        }
        return parseEnv(stdout);
    }

    protected Logger getLogger() {
        return logger;
    }

    public String getEnv(String name) throws IOException {
        Map<String, String> environments = getEnv();
        return environments.get(name);
    }

    Map<String, String> parseEnv(String stdout) {
        Map<String, String> environments = new HashMap<>();
        if(StringUtils.isEmpty(stdout))
            return environments;
        String[] split = stdout.split("\n");
        for (String env : split) {
            if (env.contains(JSCH_EXIT_CODE_0)) {
                break;
            }
            //
            int index = env.indexOf('=');
            if (!env.contains(JSCH_EXIT_CODE_MARKER) && index >= 0) {
                String name = env.substring(0, index);
                String value = env.substring(index + 1);
                if (StringUtils.isNotEmpty(value)) {
                    if (value.charAt(value.length() - 1) == '\r') {
                        value = value.substring(0, value.length() - 1);
                    }
                }
                environments.put(name, value);
            }
        }
        return environments;
    }

    public void setEnv(String name, String value) throws IOException {
        sendCommand("export " + name + "=" + value);
        envs.put(name, value);
    }

    public void unsetEnv(String name) throws IOException {
        sendCommand("unset " + name);
        envs.put(name, null);
    }

    public void resetEnv() {
        for (Map.Entry<String, String> env : envs.entrySet()) {
            String name = env.getKey();
            String value = env.getValue();
            String defaultValue = defaultEnvs.get(name);
            if (defaultValue == null && value != null) {
                try {
                    unsetEnv(env.getKey());
                } catch (Exception ignore) {
                    // Ignore
                }
            } else if (defaultValue != null && !defaultValue.equals(value)) {
                try {
                    setEnv(name, defaultValue);
                } catch (Exception ignore) {
                    // Ignore
                }
            }
        }
        envs.clear();
    }

    public String read() throws IOException {
        return read(timeOutSeconds);
    }

    public String read(long timeOutSeconds) throws IOException {
        return spawnHelper.read(timeOutSeconds);
    }

    public String readErr() throws IOException {
        return readErr(timeOutSeconds);
    }

    public String readErr(long timeOutSeconds) throws IOException {
        return spawnHelper.readErr(timeOutSeconds);
    }

}
