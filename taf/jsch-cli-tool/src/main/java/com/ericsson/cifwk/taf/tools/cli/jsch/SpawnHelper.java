package com.ericsson.cifwk.taf.tools.cli.jsch;

import static java.lang.String.format;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.Selector;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.ericsson.cifwk.taf.tools.cli.terminal.parser.TerminalParser;
import com.google.common.base.Throwables;

public class SpawnHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(expectj.Spawn.class);

    private static final String END_OF_STREAM_EXCEPTION_MESSAGE = "End of stream reached, no match found";
    private static final String UNABLE_TO_MATCH_PATTER_EXCEPTION_MESSAGE_TEMPLATE = "Timeout trying to match '%s'";

    expectj.Spawn spawn;

    Field stdoutSelector;

    Field continueReading;

    Field stderrSelector;

    TerminalParser parser;

    public SpawnHelper(expectj.Spawn spawn, TerminalParser parser) {
        this.spawn = spawn;
        this.parser = parser;
    }

    SpawnHelper init() {
        disableEchoOutToLocalSystemOut();
        return this;
    }

    /**
     * remove echo output to local System.out
     */
    void disableEchoOutToLocalSystemOut() {
        try {
            Object spawnableHelper = getField(spawn, "slave");
            Object outStreamPiper = getField(spawnableHelper, "spawnOutToSystemOut");
            setField(outStreamPiper, "pipingPaused", true);
            Object errStreamPiper = getField(spawnableHelper, "spawnErrToSystemErr");
            setField(errStreamPiper, "pipingPaused", true);
        } catch (Exception ex) {
            LOGGER.error("Exception\n", ex);
        }
    }

    Selector stdoutSelector() {
        try {
            if (stdoutSelector == null) {
                stdoutSelector = getField("stdoutSelector");
            }
            return (Selector) stdoutSelector.get(spawn);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

    Selector stderrSelector() {
        try {
            if (stderrSelector == null) {
                stderrSelector = getField("stderrSelector");
            }
            return (Selector) stderrSelector.get(spawn);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

    boolean continueReading() {
        try {
            if (continueReading == null) {
                continueReading = getField("continueReading");
            }
            return (boolean) continueReading.get(spawn);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

    void continueReading(boolean continueReading) {
        try {
            if (this.continueReading == null) {
                this.continueReading = getField("continueReading");
            }
            this.continueReading.set(spawn, continueReading);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

    Field getField(String name) throws NoSuchFieldException {
        return getField(spawn.getClass(), name);
    }

    Field getField(Class klass, String name) throws NoSuchFieldException {
        Field field = klass.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    <T> T getField(Object obj, String name) throws NoSuchFieldException, IllegalAccessException {
        return (T) getField(obj.getClass(), name).get(obj);
    }

    <T> void setField(Object obj, String name, T value) throws NoSuchFieldException, IllegalAccessException {
        getField(obj.getClass(), name).set(obj, value);
    }

    /**
     * Wait for a string to appear on standard out.
     *
     * @param string         The case-sensitive substring to match against.
     * @param timeOutSeconds The timeout in seconds before the match fails.
     * @throws IOException      on IO trouble waiting for pattern
     * @throws TimeoutException on timeout waiting for pattern
     */
    public String expect(String string, long timeOutSeconds) throws IOException, TimeoutException {
        return expect(new Expect(string), timeOutSeconds, stdoutSelector());
    }

    /**
     * Wait for a pattern to appear on standard out.
     *
     * @param pattern        The pattern to match against.
     * @param timeOutSeconds The timeout in seconds before the match fails.
     * @throws IOException      on IO trouble waiting for pattern
     * @throws TimeoutException on timeout waiting for pattern
     */
    public String expect(Pattern pattern, long timeOutSeconds) throws IOException {
        return expect(new Expect(pattern), timeOutSeconds, stdoutSelector());
    }

    String expect(Expect expect, long lTimeOutSeconds, Selector selector) throws IOException, TimeoutException {
        checkTimeOutAndSelectorKeysSize(lTimeOutSeconds, selector);
        LOGGER.debug("Expecting '" + expect.value() + "'");
        StringBuilder out = new StringBuilder();
        StringBuilder result = new StringBuilder();

        StringBuilder multipleLineSearch = new StringBuilder();
        CircularBuffer<String> stdOutLog = new CircularBuffer<>(20);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int maxExpectLength = 200;

        // If this cast fails somebody gave us the wrong selector.
        Pipe.SourceChannel readMe = getChannel(selector);
        Date runUntil = runUntil(lTimeOutSeconds);
        boolean found = false;
        continueReading(true);
        while (continueReading()) {
            if (runUntil == null) {
                selector.select();
            } else {
                long msLeft = runUntil.getTime() - new Date().getTime();
                if (msLeft > 0) {
                    selector.select(msLeft);
                } else {
                    continueReading(false);
                    break;
                }
            }
            if (selector.selectedKeys().size() == 0) {
                // Woke up with nothing selected, try again
                continue;
            }

            buffer.rewind();
            String str = getParsedString(expect, stdOutLog, buffer, readMe);

            LOGGER.debug("RESULT: " + str);
            //
            out.append(str);
            result.append(str);
            //
            found = filterThroughLinesForAMatch(expect, out.toString().split("\r\n"), multipleLineSearch, stdOutLog, maxExpectLength);
            if (found)
                break;

            while (out.indexOf("\n") != -1) {
                out.delete(0, out.indexOf("\n") + 1);
            }

            while (out.length() > maxExpectLength) {
                out.delete(0, out.length() - maxExpectLength);
            }
        }
        if (found) {
            LOGGER.debug("Match found, continueReading=" + continueReading());
        } else {
            LOGGER.debug("Timed out waiting for match, continueReading=" + continueReading());
        }
        unableToMatchPatternException(expect, stdOutLog);
        return result.toString();
    }

    private void checkTimeOutAndSelectorKeysSize(final long lTimeOutSeconds, final Selector selector) {
        if (lTimeOutSeconds < -1) {
            throw new IllegalArgumentException("Timeout must be >= -1, was " + lTimeOutSeconds);
        }
        if (selector.keys().size() != 1) {
            throw new IllegalArgumentException("Selector key set size must be 1, was " + selector.keys().size());
        }
    }

    private Pipe.SourceChannel getChannel(final Selector selector) {
        return (Pipe.SourceChannel) ((selector.keys().iterator().next())).channel();
    }

    private Date runUntil(final long lTimeOutSeconds) {
        return lTimeOutSeconds > 0 ? new Date(new Date().getTime() + lTimeOutSeconds * 1000) : null;
    }

    private String getParsedString(final Expect expect, final CircularBuffer<String> stdOutLog, final ByteBuffer buffer, final Pipe.SourceChannel readMe)
    throws IOException {
        threadSleep();
        int readCount = readMe.read(buffer);
        validateReadCount(expect.toString(), stdOutLog.toString(), readCount);
        buffer.rewind();
        return parseBytes(buffer, readCount);
    }

    private void threadSleep() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            LOGGER.trace("Sleep exception: " + e);
        }
    }

    private int validateReadCount(String expectedValue, String stdOutLog, int readCount)
    throws IOException {

        if (readCount == -1) {
            String errorMessage = createTimeoutExceptionMessage(expectedValue, stdOutLog, StringUtils.EMPTY);
            if (!errorMessage.isEmpty()) {
                LOGGER.error(errorMessage);
            }
            LOGGER.error(END_OF_STREAM_EXCEPTION_MESSAGE);
            throw new IOException(END_OF_STREAM_EXCEPTION_MESSAGE);
        }
        return readCount;
    }

    private String createTimeoutExceptionMessage(String matcher, String stdOutLog, String defaultMessage) throws IOException {
        String errorLog = readErr(5);
        if (!stdOutLog.isEmpty() || !errorLog.isEmpty()) {
            errorLog = errorLog.isEmpty() ? " was empty" : errorLog;
            return formatTimeoutEmptyLogExceptionMessage(matcher, stdOutLog, errorLog);
        } else {
            return defaultMessage;
        }
    }

    private String parseBytes(final ByteBuffer buffer, final int readCount) throws UnsupportedEncodingException {
        String str = new String(buffer.array(), buffer.arrayOffset(), readCount, "ISO-8859-1");
        if (parser != null)
            str = parser.parse(str);
        return str;
    }

    private boolean filterThroughLinesForAMatch(final Expect expect, final String[] lines, final StringBuilder multipleLineSearch,
            final CircularBuffer<String> stdOutLog, final int maxExpectLength) {
        boolean found = false;
        for (String line : lines) {
            stdOutLog.add("\n" + line);
            multipleLineSearch.append(line.replaceAll(" ", ""));
            if (expect.in(line) || expect.in(multipleLineSearch.toString())) {
                LOGGER.debug("Found match for " + expect.value() + ":" + line);
                found = true;
                break;
            } else {
                if (multipleLineSearch.length() > maxExpectLength) {
                    multipleLineSearch.delete(0, multipleLineSearch.length() - maxExpectLength);
                }
            }
        }
        return found;
    }

    private void unableToMatchPatternException(final Expect expect, final CircularBuffer<String> stdOutLog) throws IOException {
        if (!continueReading()) {
            String defaultMessage = format(UNABLE_TO_MATCH_PATTER_EXCEPTION_MESSAGE_TEMPLATE, expect.value());
            String errorMessage = createTimeoutExceptionMessage(expect.value(), stdOutLog.toString(), defaultMessage);
            throw new TimeoutException(errorMessage);
        }
    }

    private String formatTimeoutEmptyLogExceptionMessage(String matcher, String stdOutLog, String errorLog) {
        return new StringBuilder()
                .append(format(UNABLE_TO_MATCH_PATTER_EXCEPTION_MESSAGE_TEMPLATE, matcher))
                .append(System.lineSeparator())
                .append(format("Console output prior to timeout: '%s'", stdOutLog))
                .append(System.lineSeparator())
                .append(format("StdError prior to timeout: '%s'", errorLog))
                .toString();
    }

    /**
     * Read a text from standard out.
     *
     * @param timeOutSeconds The timeout in seconds before stop reading.
     * @throws IOException on IO trouble reading
     */
    public String read(long timeOutSeconds) throws IOException {
        Selector selector = stdoutSelector();
        if (selector == null)
            return "";
        return read(timeOutSeconds, selector);
    }

    /**
     * Read a text from standard Err.
     *
     * @param timeOutSeconds The timeout in seconds before stop reading.
     * @throws IOException on IO trouble reading
     */
    public String readErr(long timeOutSeconds) throws IOException {
        Selector selector = stderrSelector();
        if (selector == null)
            return "";
        return read(timeOutSeconds, selector);
    }

    String read(long timeOutSeconds, Selector selector) throws IOException {
        checkTimeOutAndSelectorKeysSize(timeOutSeconds, selector);
        StringBuilder result = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // If this cast fails somebody gave us the wrong selector.
        Pipe.SourceChannel readMe = getChannel(selector);
        long runUntil = System.currentTimeMillis() + (timeOutSeconds > 0 ? (timeOutSeconds * 1000) : 100);
        continueReading(true);
        while (continueReading()) {
            long msLeft = runUntil - System.currentTimeMillis();
            if (msLeft > 0) {
                selector.select(msLeft);
            } else {
                continueReading(false);
                break;
            }
            if (selector.selectedKeys().size() == 0) {
                continue;
            }
            buffer.rewind();
            int readCount = readMe.read(buffer);
            if (readCount == -1 || readCount == 0) {
                break;
            }
            buffer.rewind();
            String str = new String(buffer.array(), buffer.arrayOffset(), readCount, "ISO-8859-1");
            if (parser != null)
                str = parser.parse(str);
            result.append(str);
        }
        return result.toString();
    }
}
