package com.ericsson.cifwk.taf.tools.cli;

import com.ericsson.cifwk.taf.data.Host;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.ericsson.cifwk.taf.tools.cli.CLICommandHelperConstants.*;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommandHelperTest {

    protected static final String SAMPLE_RESPONSE = "getent hosts httpd-instance-1 | /usr/bin/awk '{print $1}'\r\n%s[root@cloud-ms-1 ~]# ";

    @Mock
    private Shell mockedShell;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testPatternUsedInSplitStdOutWorksCorrectly() {
        CLICommandHelper cliCommandHelper = new CLICommandHelper();
        String[] output = cliCommandHelper.splitStdOut("test \r is removed\nte\rst is also removed\ntrailing whitespace     \r is removed");
        assertThat(Arrays.asList(output).get(0)).isEqualTo("test is removed");
        assertThat(Arrays.asList(output).get(1)).isEqualTo("test is also removed");
        assertThat(Arrays.asList(output).get(2)).isEqualTo("trailing whitespace is removed");
    }

    @Test
    public void extractIpFromEtcHostFilePass() {
        String ipAddress = "10.247.246.158" + System.lineSeparator();
        assertThat(extractIpFromFile(ipAddress)).matches(ipAddress.trim());
    }

    @Test
    public void extractIpFromEtcHostFileFail() {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage(CLICommandHelperConstants.FAILED_TO_GET_IP_FROM_ETC_HOST_FILE_ERR_MSG);
        String ipAddress = "";
        extractIpFromFile(ipAddress);
    }

    private String extractIpFromFile(String ipAddress) {
        Host testHost = new Host.HostBuilder().withName("testHost").build();
        CLICommandHelper cliCommandHelper = new CLICommandHelper();
        cliCommandHelper.shell = mockedShell;
        String mockedResponse = String.format(SAMPLE_RESPONSE, ipAddress);
        when(mockedShell.expect(CLICommandHelperConstants.GENERIC_PROMPT_PATTERN)).thenReturn(mockedResponse);
        return cliCommandHelper.newHopBuilder().resolveIpFromCache(testHost);
    }

    @Test
    public void cleanResponseFromEtcHostFileIpv4() {
        cleanResponseFromEtcHostFile("10.247.246.158" + System.lineSeparator());
    }

    @Test
    public void cleanResponseFromEtcHostFileIpv6() {
        cleanResponseFromEtcHostFile("2001:1b70:82a1:103::196" + System.lineSeparator());
    }

    private void cleanResponseFromEtcHostFile(String ipaddress) {
        CLICommandHelper cliCommandHelper = new CLICommandHelper();
        String response = String.format(SAMPLE_RESPONSE, ipaddress);
        String result = cliCommandHelper.newHopBuilder().extractFirstIpAddressFromResponse(response);
        assertThat(result).matches(ipaddress.trim());
    }

    @Test
    public void userPasswordPromptRegexTest() {
        Pattern passwordPromptPattern = Pattern.compile(USER_PASSWORD_PROMPT + String.format(USER_NAME_PATTERN, "root"));
        assertThat(passwordPromptPattern.matcher("su - root").matches()).isFalse();
        assertThat(passwordPromptPattern.matcher(" ").matches()).isFalse();
        assertThat(passwordPromptPattern.matcher("########").matches()).isFalse();
        assertThat(passwordPromptPattern.matcher("#WARNING#").matches()).isFalse();
        assertThat(passwordPromptPattern.matcher("# WARNING #").matches()).isFalse();
        assertThat(passwordPromptPattern.matcher("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@").matches()).isFalse();
        assertThat(passwordPromptPattern.matcher("@    WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!     @").matches()).isFalse();
        assertThat(passwordPromptPattern.matcher("RSA host key for 10.247.246.23 has changed and you have requested strict checking.").matches()).isFalse();
        assertThat(passwordPromptPattern.matcher("[root@svc1]$").matches()).isTrue();
        assertThat(passwordPromptPattern.matcher("[root@ieatlms4421-1 ~]#").matches()).isTrue();
        assertThat(passwordPromptPattern.matcher("su - root\n[root@svc1]$").matches()).isTrue();
        assertThat(passwordPromptPattern.matcher("Password:").matches()).isTrue();
        //genericHashPrompt
        assertThat(passwordPromptPattern.matcher("#").matches()).isFalse();
        assertThat(passwordPromptPattern.matcher("\nAnyCharsExceptHash#").matches()).isTrue();
        //genericAnglePrompt
        assertThat(passwordPromptPattern.matcher(">").matches()).isFalse();
        assertThat(passwordPromptPattern.matcher("\nAnyCharsExceptAngleBracket>").matches()).isTrue();
        //genericDollarPrompt
        assertThat(passwordPromptPattern.matcher("$").matches()).isTrue();
        assertThat(passwordPromptPattern.matcher("test$").matches()).isTrue();
    }

    @Test
    public void initialLoginPatternRegexTest() {
        //INITIAL_LOGIN_PATTERN
        String initialLoginString = ".*"+INITIAL_LOGIN_PATTERN + ".*";
        initialLoginString = initialLoginString.replaceAll("\\|", ".*\\|.*");
        Pattern initialLoginPattern = Pattern.compile(initialLoginString);

        assertThat(initialLoginPattern.matcher("Are you sure").matches()).isTrue();
        assertThat(initialLoginPattern.matcher("Are you sure?").matches()).isTrue();
        assertThat(initialLoginPattern.matcher("Are you really sure").matches()).isFalse();
        assertThat(initialLoginPattern.matcher("password").matches()).isTrue();
        assertThat(initialLoginPattern.matcher("Password:").matches()).isTrue();
        assertThat(initialLoginPattern.matcher("passw0rd").matches()).isFalse();
        assertThat(initialLoginPattern.matcher("No route to host").matches()).isTrue();
        assertThat(initialLoginPattern.matcher("route to host").matches()).isFalse();
        assertThat(initialLoginPattern.matcher("$").matches()).isTrue();
        assertThat(initialLoginPattern.matcher("[root@ieatlms4421-1 ~]#").matches()).isTrue();
        //genericHashPrompt
        assertThat(initialLoginPattern.matcher("#").matches()).isFalse();
        assertThat(initialLoginPattern.matcher("\nAnyCharsExceptHash#").matches()).isTrue();
        //genericAnglePrompt
        assertThat(initialLoginPattern.matcher(">").matches()).isFalse();
        assertThat(initialLoginPattern.matcher("\nAnyCharsExceptAngleBracket>").matches()).isTrue();
    }
}
