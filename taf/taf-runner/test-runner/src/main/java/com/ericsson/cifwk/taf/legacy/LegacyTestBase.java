package com.ericsson.cifwk.taf.legacy;

import com.ericsson.cifwk.meta.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated
 * Legacy test reporting and synchronization from <code>se.ericsson.jcat.fw.ng.JcatNGTestBase</code>.
 */
@Deprecated
@API(API.Quality.Deprecated)
@API.Since(2.32)
public class LegacyTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(LegacyTestBase.class);

    private static final String UNSUPPORTED = "Method is not supported";

    private static final String UNSUPPORTED_RESULT = "UNKNOWN-Method_is_not_supported";

    /**
     * Set the current test case name.
     *
     * @param curTcName The name of current test case.
     */
    @Deprecated
    public static void setCurTcName(String curTcName) {}

    @Deprecated
    public static void addParaFailureEvent(String type, String message) {
        LOG.error(UNSUPPORTED);
    }

    @Deprecated
    public void setAdditionalResultInfo(String additionalResultInfo) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Get method for assert reason.
     *
     * @return the reason for a testcase failing. Currently only the last
     *         message.
     */
    @Deprecated
    public String getAssertReason() {
        LOG.error(UNSUPPORTED);
        return UNSUPPORTED_RESULT;
    }

    /**
     * Some testcases might want to be broken down into test steps for
     * readability, a TestStep can be used for this.
     * <p>
     * TestStep is used at the same place that an Action/Result pair would be used in a manual
     * testcase.
     *
     * @param step
     *        String containing the information about the test step.
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public void setTestStep(String step) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Some testcases might want to be broken down into test steps for
     * readability, a TestStep can be used for this.
     * <p>
     * TestStep is used at the same place that an Action/Result pair would be used in a manual
     * testcase. This method starts the testStep.
     *
     * @param step
     *        String containing the information about the test step.
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public void setTestStepBegin(String step) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * This is used to stop grouping the actions in a testStep. This method is
     * intended to close an opened test step. If there is no any test step
     * opened, this methods does nothing.
     *
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public void setTestStepEnd() {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Some testcases might want to be broken down into test steps for
     * readability, a TestStep can be used for this.
     * <p>
     * TestStep is used at the same place that an Action/Result pair would be used in a manual
     * testcase.
     *
     * @param step
     *        String containing the information about the test step.
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public void setSubTestStep(String step) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * This method will generate a log file in the log directory. If the log
     * directory does not exist the method will create it under html log
     * directory (LogParser2.dateLogDir).
     *
     * @param name
     *        - The name of the log file.
     * @param content
     *        - The content of the log file.
     * @return - The html link to the log file.
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public String setTestFile(String name, String content) {
        LOG.error(UNSUPPORTED);
        return UNSUPPORTED_RESULT;
    }

    /**
     * This method will generate a log file in the log directory. If the log
     * directory does not exist the method will create it under html log
     * directory (LogParser2.dateLogDir).
     *
     * @param name
     *        - The name of the log file.
     * @param content
     *        - The content of the log file.
     * @param logDirName
     *        - The name of the log directory.
     * @return - The html link to the log file.
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public String setTestFile(String name, String content, String logDirName) {
        LOG.error(UNSUPPORTED);
        return UNSUPPORTED_RESULT;
    }

    /**
     * This method will generate a log file in the log directory. If the log
     * directory does not exist the method will create it under html log
     * directory (LogParser2.dateLogDir).
     *
     * @param name
     *        - The name of the log file.
     * @param content
     *        - The content of the log file.
     * @param isTempFile
     *        - The flag to identify if add random number in the file name.
     * @return - The html link to the log file.
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public String setTestFile(String name, String content, boolean isTempFile) {
        LOG.error(UNSUPPORTED);
        return UNSUPPORTED_RESULT;
    }

    /**
     * This method will generate a log file in the log directory. If the log
     * directory does not exist the method will create it under html log
     * directory (LogParser2.dateLogDir).
     *
     * @param name
     *        - The name of the log file.
     * @param content
     *        - The content of the log file.
     * @param logDirName
     *        - The name of the log directory.
     * @param isTempFile
     *        - The flag to identify if add random number in the file name.
     * @return - The html link to the log file.
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public String setTestFile(String name, String content, String logDirName, boolean isTempFile) {
        LOG.error(UNSUPPORTED);
        return UNSUPPORTED_RESULT;
    }

    /**
     * This method will generate a log file in the log directory.
     * If the log directory does not exist the method will create it under html
     * log directory (LogParser2.dateLogDir).
     *
     * @param name - The name of the log file.
     * @param contents - The content of the log file.
     * @param suffix - The suffix of the log file.
     * @return - The html link to the log file.
     * @throws UnsupportedOperationException if test case is not executing
     */
    @Deprecated
    public String setTestFileWithSuffix(String name, String[] contents, String suffix) {
        LOG.error(UNSUPPORTED);
        return UNSUPPORTED_RESULT;
    }

    /**
     * This method will generate a log file in the log directory.
     * If the log directory does not exist the method will create it under html
     * log directory (LogParser2.dateLogDir).
     *
     * @param name - The name of the log file.
     * @param contents - The content of the log file.
     * @param logDirName - The name of the log directory.
     * @param suffix - The suffix of the log file.
     * @return - The html link to the log file.
     * @throws UnsupportedOperationException if test case is not executing
     */
    @Deprecated
    public String setTestFileWithSuffix(String name, String[] contents, String logDirName,
                                        String suffix) {
        LOG.error(UNSUPPORTED);
        return UNSUPPORTED_RESULT;
    }

    /**
     * This method will generate a log file in the log directory.
     * If the log directory does not exist the method will create it under html
     * log directory (LogParser2.dateLogDir).
     *
     * @param name - The name of the log file.
     * @param content - The content of the log file.
     * @param suffix - The suffix of the log file.
     * @return - The html link to the log file.
     * @throws UnsupportedOperationException if test case is not executing
     */
    @Deprecated
    public String setTestFileWithSuffix(String name, String content, String suffix) {
        LOG.error(UNSUPPORTED);
        return UNSUPPORTED_RESULT;
    }

    /**
     * This method will generate a log file in the log directory.
     * If the log directory does not exist the method will create it under html
     * log directory (LogParser2.dateLogDir).
     *
     * @param name - The name of the log file.
     * @param content - The content of the log file.
     * @param logDirName - The name of the log directory.
     * @param suffix - The suffix of the log file.
     * @return - The html link to the log file.
     * @throws UnsupportedOperationException if test case is not executing
     */
    @Deprecated
    public String setTestFileWithSuffix(String name, String content, String logDirName,
                                        String suffix) {
        LOG.error(UNSUPPORTED);
        return UNSUPPORTED_RESULT;
    }

    /**
     * TestInfo is any other information that needs to be logged in a testcase
     * that is not a heading or teststep.
     *
     * @param info
     *        String containing the information.
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public void setTestInfo(String info) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Show a formatted info.
     */
    @Deprecated
    public void setTestInfo(String format, Object... obj) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * TestInfo is any other information that needs to be logged in a testcase
     * that is not a heading or teststep.
     *
     * @param info
     *        String containing the information.
     * @param t
     *        An exception object.
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public void setTestInfo(String info, Throwable t) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Inserts a debug message the logs. Itt will be displayed with Level.DEBUG
     * in the HTML logs.
     *
     * @param info String containing the information.
     */
    @Deprecated
    public void setTestDebug(String info) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Inserts a debug message the logs with an exception object.
     * It will be displayed with Level.DEBUG in the HTML logs.
     *
     * @param info String containing the information.
     * @param t An exception object
     */
    @Deprecated
    public void setTestDebug(String info, Throwable t) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Show a formatted debug info.
     */
    @Deprecated
    public void setTestDebug(String format, Object... obj) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Inserts a warning message to the logs. Itt will be displayed with
     * Level.WARNING in the HTML logs.
     *
     * @param info String containing the information.
     */
    @Deprecated
    public void setTestWarning(String info) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Inserts a warning message to the logs with an exception object.
     * Itt will be displayed with Level.WARNING in the HTML logs.
     *
     * @param info String containing the information.
     * @param t An exception object
     */
    @Deprecated
    public void setTestWarning(String info, Throwable t) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Show a formatted warning info
     */
    @Deprecated
    public void setTestWarning(String format, Object... obj) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Inserts a trace message the logs. Itt will be displayed with Level.TRACE
     * in the HTML logs.
     */
    @Deprecated
    public void setTestTrace(String info) {
        LOG.error(UNSUPPORTED);
    }

    @Deprecated
    public void setTestTrace(String info, Throwable t) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Show a formatted trace info
     */
    @Deprecated
    public void setTestTrace(String format, Object... obj) {
        LOG.error(UNSUPPORTED);
    }

    @Deprecated
    public void setTestHide(String info) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Inserts a failure message the logs with an exception object.
     * Itt will be displayed with Level.ERROR in the HTML logs.
     *
     * @param info String containing the information.
     * @param t An exception object
     */
    @Deprecated
    public void setTestFailure(String info, Throwable t) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Show a formatted error info
     */
    @Deprecated
    public void setTestFailure(String format, Object... obj) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * TestInfo is any other information that needs to be logged in a testcase
     * that is not a heading or teststep.
     *
     * @param info
     *        String[] containing the information.
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public void setTestInfo(String[] info) {
        LOG.error(UNSUPPORTED);
    }

    @Deprecated
    public void setTestInfoPre(String info) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Log the information about the testcase, this includes the testcase id and
     * the heading.
     *
     * @param id
     *        String containing the tcid.
     * @param heading
     *        String containing the heading.
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public void setTestcase(String id, String heading) {
        LOG.error(UNSUPPORTED);
    }

    @Deprecated
    public void setTestcase() {
        LOG.error(UNSUPPORTED);
    }

    /**
     * @deprecated add some info at the end of testcase .
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public void setTestSummary() {
        LOG.error(UNSUPPORTED);
    }

    @Deprecated
    public void setTestSummary(Logger logger) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * TestFailure is information about why a testcase failed. It will be logged
     * at Level.Error.
     *
     * @param info
     *        String containing the information.
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public void setTestFailure(String info) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Log the information about a sub testcase, this includes the testcase id
     * and the heading.
     *
     * @param id
     *        String containing the tcid.
     * @param heading
     *        String containing the heading.
     * @throws UnsupportedOperationException
     *         if test case is not executing
     */
    @Deprecated
    public void setSubTestcase(String id, String heading) {
        LOG.error(UNSUPPORTED);
    }

    @Deprecated
    public void enableStopOnFailure() {
        LOG.error(UNSUPPORTED);
    }

    @Deprecated
    public void disableStopOnFailure() {
        LOG.error(UNSUPPORTED);
    }

    @Deprecated
    public void showEvents() {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Waits until all expected events have arrived for max 1200 seconds. If
     * expected events do not arrive the test case is marked as fail.
     *
     * @return synchronize result.
     */
    @Deprecated
    public boolean synchronizeEvents() {
        LOG.error(UNSUPPORTED);
        return true;
    }

    /**
     * Waits until all expected events have arrived for max the specified time.
     * If expected events do not arrive the test case is marked as fail.
     *
     * @param maxTime
     *        maximum time to wait for expected events.
     * @return synchronize result.
     */
    @Deprecated
    public boolean synchronizeEvents(int maxTime) {
        LOG.error(UNSUPPORTED);
        return true;
    }

    /**
     * Waits until all expected events have arrived for max the specified time.
     * After all expected events are received, it will sleep for the specified
     * time. If expected events do not arrive the test case is marked as fail.
     *
     * @param maxTime
     *        maximum time to wait for expected events.
     * @param sleepTime
     *        time to sleep after all expected events are recieved.
     * @return synchronize result.
     */
    @Deprecated
    public boolean synchronizeEvents(int maxTime, int sleepTime) {
        LOG.error(UNSUPPORTED);
        return true;
    }

    /*
     * Clear events for all connected ZonesHolder.getInstance().zones.
     */
    @Deprecated
    public void clearEvents() {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Gets the named property from <code>Cat2Utils</code>, which holds any
     * properties defined with the <code>-Dprops</code> flag.
     *
     * @param name
     *        property name
     * @return property value
     */
    @Deprecated
    public String getTestProperty(String name) {
        LOG.error(UNSUPPORTED);
        return UNSUPPORTED_RESULT;
    }

    /**
     * Functionality under construction.
     *
     * @param type
     *        traffic type.
     * @param maxTime
     *        maximum wait time.
     * @return operation result.
     */
    @Deprecated
    public boolean trafficRecovered(String[] type, int maxTime,
                                    float discrepancy) {
        LOG.error(UNSUPPORTED);
        return false;
    }

    /**
     * Functionality under construction
     *
     * @param type
     *        traffic type(s).
     * @return operation result.
     */
    @Deprecated
    public boolean setTrafficIntensity(String[] type) {
        LOG.error(UNSUPPORTED);
        return false;
    }

    /**
     * Set the start time for characteristics delta measurements
     *
     * @param charName
     *        String of characterics name. ie BoardRestart, NodeRestart etc
     */
    @Deprecated
    public void startCharTime(String charName) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Set the delta time for characteristics delta measurements
     *
     * @param charName
     *        String of characterics name. ie BoardRestart, NodeRestart etc
     * @param tcName
     *        String of testcase name. ie TC-ROBRESTART-001 etc
     * @param measure
     *        String of what is being measured. ie "JVM started"
     *        "Total Functionality" etc, Many measurements can be made per
     *        testcase
     */
    @Deprecated
    public void setCharTimeDelta(String charName, String tcName,
                                 String measure) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Set the time for characteristics where time is known already
     *
     * @param charName
     *        String of characterics name. ie BoardRestart, NodeRestart etc
     * @param tcName
     *        String of testcase name. ie TC-ROBRESTART-001 etc
     * @param measure
     *        String of what is being measured. ie "JVM started"
     *        "Total Functionality" etc, Many measurements can be made per
     *        testcase
     * @param time
     *        String of the time that the event occured. in format jvm time
     *        in seconds
     */
    @Deprecated
    public void setCharTime(String charName, String tcName, String measure,
                            long time) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Set the measured value in DB log
     *
     * @param charName The specified characteristics name. E.g., "Duration"
     * @param measure The specified measure name. E.g., "End Test"
     * @param value The time of the relevant measure, normally, the current
     *        system time in the unit
     *        of SECOND.
     * @param unit The time unit
     */
    @Deprecated
    public void setMeasuredValue(String charName, String measure,
                                 float value, String unit) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Tag test case with product specific property (key-value pair)
     */
    @Deprecated
    public void setTcProperty(String propertyName, String propertyValue) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Tag test suite with product specific property (key-value pair)
     */
    @Deprecated
    public void setTsProperty(String propertyName, String propertyValue) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Start a new file to store the log information for the current test case.<br/>
     * NB: if you called the methods, please do not forget to call the method
     * {@link #stopTestCaseLogInNewPage()}, and the two methods should be called
     * in pairs<br/>
     *
     * @param pageName - the name of the new page
     * @see #stopTestCaseLogInNewPage
     */
    @Deprecated
    protected void setTestCaseLogInNewPage(String pageName) {
        LOG.error(UNSUPPORTED);
    }

    /**
     * Stop to logging the message to the new page for the current test case,
     * and incoming log will
     * be logged into the parent page. <br>
     * NB:This method should be called after the function call of
     * {@link #setTestCaseLogInNewPage(String)}
     *
     * @see #setTestCaseLogInNewPage(String)
     */

    @Deprecated
    protected void stopTestCaseLogInNewPage() {
        LOG.error(UNSUPPORTED);
    }

}
