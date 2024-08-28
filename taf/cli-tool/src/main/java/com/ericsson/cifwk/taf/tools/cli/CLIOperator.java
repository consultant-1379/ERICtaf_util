package com.ericsson.cifwk.taf.tools.cli;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.configuration.TafDataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.utils.InternalFileFinder;
import com.google.common.base.Throwables;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

@API(Deprecated)
@API.Since(2.30)
@Deprecated
public class CLIOperator {

    static final String JSCH_EXIT_CODE_MARKER = "taf.jsch.exitcode";
    static final String ECHO_EXIT_CODE_CMD = "echo \"" + JSCH_EXIT_CODE_MARKER + "=$?\"";

    Map<String, Shell> shells = new HashMap<>();
    Map<String, String> paths = new HashMap<>();

    public class Result {

        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        int exitCode;

        public Result(String cmd, String out, String err) {
            boolean isOutBegin = false;
            boolean isOutStop = false;
            //
            for (String str : out.split("[\\n,\\r\\n]+")) {
                if (isOutStop && str.matches(JSCH_EXIT_CODE_MARKER + "=\\d*.*")) {
                    String value = str.substring(str.indexOf('=') + 1);
                    exitCode = Integer.parseInt(value);
                    break;
                }
                if (!isOutBegin && str.contains(cmd)) {
                    isOutBegin = true;
                    continue;
                }
                if (str.contains(ECHO_EXIT_CODE_CMD)) {
                    isOutStop = true;
                    continue;
                }
                if (isOutBegin && !isOutStop) {
                    stdout.add(str);
                }
            }
            //
            stderr = new ArrayList<>();
            for (String str : err.split("[\\n,\\r\\n]+")) {
                stderr.add(str);
            }
        }

        public String getStdOut() {
            StringBuilder stdout = new StringBuilder();
            for (String str : this.stdout) {
                stdout.append(str).append("\n");
            }
            return stdout.toString();
        }

        public String getStdErr() {
            StringBuilder stderr = new StringBuilder();
            for (String str : this.stderr) {
                stderr.append(str).append("\n");
            }
            return stderr.toString();
        }


        public int getExitCode() {
            return exitCode;
        }

    }

    public Map<String, String> loadData(String location, String step) {
        try {
            String path = paths.get(location);
            if (path == null) {
                path = InternalFileFinder.findFile(location);
                paths.put(location, path);
            }
            CsvMapReader csvReader = new CsvMapReader(new FileReader(path), CsvPreference.EXCEL_PREFERENCE);
            String[] headers = csvReader.getHeader(true);
            Map<String, String> read = csvReader.read(headers);
            while (read != null) {
                if (step.equalsIgnoreCase(read.get("string"))) {
                    return read;
                }
                read = csvReader.read(headers);
            }
            return Collections.emptyMap();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public Result execute(String command, Map<String, String> args) {
        Shell shell = getShell(args);
        String cmd = createCmd(command, args);
        return execute(shell, cmd, args);
    }


    protected Result execute(Shell shell, String cmd, Map<String, String> args) {
        shell.writeln(cmd + ";" + ECHO_EXIT_CODE_CMD);
        try {
            String timeout = args.get("timeout");
            String out;
            String err;
            if (timeout != null) {
                out = shell.expect(Pattern.compile(JSCH_EXIT_CODE_MARKER + "=\\d+?"), Integer.parseInt(timeout));
                err = shell.readErr(0);
            } else {
                out = shell.expect(Pattern.compile(JSCH_EXIT_CODE_MARKER + "=\\d+?"));
                err = shell.readErr(0);
            }
            return new Result(cmd, out, err);
        } catch (TimeoutException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    protected String createCmd(String command, Map<String, String> args) {
        String cmd = TafDataHandler.getAttribute(command);
        if (args.get("args") != null) {
            cmd += " " + args.get("args");
        }
        return cmd;
    }

    protected Shell getShell(Map<String, String> args) {
        String hostName = args.get("host");
        Shell shell = shells.get(hostName);
        if (shell == null) {
            Host host = TafDataHandler.findHost().withHostname(hostName).get();
            CLI cli = new CLI(host);
            shell = cli.openShell(Terminal.VT100);
            shells.put(hostName, shell);
        }
        shell.read(0);
        shell.readErr(0);
        return shell;
    }

}
