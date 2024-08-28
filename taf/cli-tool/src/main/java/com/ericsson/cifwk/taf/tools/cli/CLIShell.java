package com.ericsson.cifwk.taf.tools.cli;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

import java.util.Map;
import java.util.regex.Pattern;

import com.ericsson.cifwk.meta.API;

/**
 * This class designed to store the {@link CLITool} and the session ID,
 * and recall methods from {@link Shell} interface to  the appropriate
 * methods in {@link CLITool}, passing in the session ID to {@link CLITool}
 * methods on each call.
 * * <p>Usage is deprecated, please use
 * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/cli-tool/">cli-tool</a> instead.</p>
 *
 * @deprecated
 */
@Deprecated
@API(Deprecated)
@API.Since(2.35)
public class CLIShell implements Shell {

    private static final char NEWLINE_SEPARATOR = '\n';

    String id;
    CLITool cliTool;

    public CLIShell(String id, CLITool cliTool) {
        this.id = id;
        this.cliTool = cliTool;
    }

    @Override
    public void disconnect() {
        cliTool.disconnect(id);
    }

    @Override
    public void write(String string) {
        cliTool.write(id, string);
    }

    @Override
    public void writeln(String string) {
        if (string.isEmpty() || string.charAt(string.length() - 1) != NEWLINE_SEPARATOR) string += NEWLINE_SEPARATOR;
        cliTool.write(id, string);
    }

    @Override
    public String expect(String subString) throws TimeoutException {
        return cliTool.expect(id, subString);
    }

    @Override
    public String expect(String subString, long timeOutSeconds) throws TimeoutException {
        return cliTool.expect(id, subString, timeOutSeconds);
    }

    @Override
    public String expect(Pattern pattern) throws TimeoutException {
        return cliTool.expect(id, pattern);
    }

    @Override
    public String expect(Pattern pattern, long timeOutSeconds) throws TimeoutException {
        return cliTool.expect(id, pattern, timeOutSeconds);
    }

    @Override
    public boolean expectErr(String pattern) throws TimeoutException {
        return cliTool.expectErr(id, pattern);
    }

    @Override
    public boolean expectErr(String pattern, long timeOutSeconds) throws TimeoutException {
        return cliTool.expectErr(id, pattern, timeOutSeconds);
    }


    @Override
    public void expectClose() throws TimeoutException {
        cliTool.expectClose(id);
    }

    @Override
    public boolean expectClose(long timeOutSeconds) throws TimeoutException {
        return cliTool.expectClose(id, timeOutSeconds);
    }


    @Override
    public boolean isClosed() {
        return cliTool.isClosed(id);
    }

    @Override
    public int getExitValue() throws TimeoutException {
        return cliTool.getExitValue(id);
    }

    @Override
    public int getExitValue(long timeOutSeconds) throws TimeoutException {
        return cliTool.getExitValue(id, timeOutSeconds);
    }

    @Override
    public void setEnv(String name, String value) {
        cliTool.setEnv(id, name, value);
    }

    @Override
    public String getEnv(String name) {
        return cliTool.getEnv(id, name);
    }

    @Override
    public Map<String, String> getEnv() {
        return cliTool.getEnv(id);
    }

    @Override
    public void unsetEnv(String name) {
        cliTool.unsetEnv(id, name);
    }

    @Override
    public void resetEnv() {
        cliTool.resetEnv(id);
    }

    @Override
    public String read() {
        return cliTool.read(id);
    }

    @Override
    public String read(long timeOutSeconds) {
        return cliTool.read(id, timeOutSeconds);
    }

    @Override
    public String readErr() {
        return cliTool.readErr(id);
    }

    @Override
    public String readErr(long timeOutSeconds) {
        return cliTool.readErr(id, timeOutSeconds);
    }

}
