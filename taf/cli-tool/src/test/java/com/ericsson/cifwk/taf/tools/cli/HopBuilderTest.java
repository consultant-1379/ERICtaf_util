package com.ericsson.cifwk.taf.tools.cli;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class HopBuilderTest {

    String username = "default_user";
    String pass = "password";
    String hostIp = "1.1.1.1";
    String command = String.format("ssh %s@%s", username, hostIp);
    int testPort = 99;

    static final String CLOUD_USERNAME = "cloud-user";
    static final String CLOUD_PASSWORD = "dummy";
    static final String ROOT_USERNAME = "root";
    static final String ROOT_PASSWORD = "passw0rd";


    @Test()
    public void retryHopMechanism() {
        Host host = mock(Host.class);
        doReturn(hostIp).when(host).getIp();
        User user = mock(User.class);
        doReturn(username).when(user).getUsername();
        doReturn(pass).when(user).getPassword();
        CLICommandHelper.HopBuilder hopBuilder = spy(new CLICommandHelper().new HopBuilder());
        doReturn(false).when(hopBuilder).verifyCorrectHost(hostIp);
        doNothing().when(hopBuilder).performHop(eq(host), anyString(), anyString(), anyString());

        try {
            hopBuilder.retryMechanism(host, user, hostIp, command);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains(String.format(CLICommandHelper.HopBuilder.FAILED_TO_HOP_TO, host)));
            verify(hopBuilder, times(2)).performHop(host, username, pass, command);
        }
    }

    @Test
    public void retryUserHopMechanism() throws Exception {
        User user = mock(User.class);
        CLICommandHelper.HopBuilder hopBuilder = spy(new CLICommandHelper().new HopBuilder());
        doReturn(false).when(hopBuilder).verifyCorrectUser(user);
        doNothing().when(hopBuilder).performUserHop(eq(user));

        try {
            hopBuilder.hop(user);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().toLowerCase().contains(CLICommandHelper.HopBuilder.FAILED_TO_HOP_TO_USER.toLowerCase()));
            verify(hopBuilder, times(5)).performUserHop(user);
        }
    }

    @Test
    public void constructSshCommand() {
        Host host = new Host.HostBuilder().withType(HostType.GATEWAY).withIp(hostIp).withSshPort(testPort).build();
        User user = mock(User.class);
        doReturn(username).when(user).getUsername();
        CLICommandHelper cmdHelper = new CLICommandHelper();
        String sshCommand = cmdHelper.new HopBuilder().constructSshCommandWithHostKeyChecking(host, user, true);
        assertTrue("Checking ssh command has something like 'user@hostname'", sshCommand.contains(String.format("%s@%s", username, hostIp)));
        assertTrue("Checking ssh command is enabling StrictHostChecking", sshCommand.contains(CLICommandHelper.HopBuilder.STRICT_HOST_KEY_CHECKING_YES));
        assertTrue("Checking ssh command contains '-p' option", sshCommand.contains(String.format("-p %s", testPort)));
        sshCommand = cmdHelper.new HopBuilder().constructSshCommandWithHostKeyChecking(host, user, false);
        assertTrue("Checking ssh command is disabling StrictHostChecking", sshCommand.contains(CLICommandHelper.HopBuilder.STRICT_HOST_KEY_CHECKING_NO));
    }

    @Test
    public void testThrowRuntimeException() throws Exception {
        String ip = "1.1.1.1";
        Host host = new Host.HostBuilder().withType(HostType.GATEWAY).withIp(ip).build();
        CLICommandHelper cmdHelper = new CLICommandHelper();
        String errorMessage = "Some exception";
        try {
            throw cmdHelper.new HopBuilder().throwHostSSHFailRuntimeException(host, errorMessage);
        } catch (Exception e) {
            if (!e.getMessage().contains("1.1.1.1") || !e.getMessage().contains("GATEWAY") || !e.getMessage().contains(errorMessage)) {
                fail(String.format("Should have thrown a RuntimeException with the message containing '%s' or 'GATEWAY'", ip));
            }
        }
    }

    @Test
    public void testGetSshPort() throws Exception {
        Host host = new Host.HostBuilder().withType(HostType.GATEWAY).withIp(hostIp).build();
        CLICommandHelper cmdHelper = new CLICommandHelper();
        String port = cmdHelper.new HopBuilder().getSshPort(host);
        Assert.assertTrue(port.equals("22"));
        host = new Host.HostBuilder().withType(HostType.GATEWAY).withIp(hostIp).withSshPort(testPort).build();
        port = cmdHelper.new HopBuilder().getSshPort(host);
        Assert.assertTrue(port.equals(String.valueOf(testPort)));
    }

    @Test
    public void testGetIpAddressOfHostnameIPv4() {
        CLICommandHelper cmdHelper = new CLICommandHelper();
        assertThat(cmdHelper.newHopBuilder().getIpAddressOfHostname("1.1.1.1")).matches("1.1.1.1");
    }

    @Test
    public void testGetIpAddressOfHostnameIPv6() {
        CLICommandHelper cmdHelper = new CLICommandHelper();
        assertThat(cmdHelper.newHopBuilder().getIpAddressOfHostname("2001:1b70:82a1:103:0000:0000:0000:194")).matches("2001:1b70:82a1:103::194");
    }

    @Test
    public void testGetIpAddressOfHostname() {
        CLICommandHelper cmdHelper = new CLICommandHelper();
        assertThat(cmdHelper.newHopBuilder().getIpAddressOfHostname("654vg54")).matches("654vg54");
    }

    @Test
    public void testReturnOperatorUser() throws IOException {
        Host host = new Host();
        host.addUser(CLOUD_USERNAME, CLOUD_PASSWORD, UserType.OPER);
        CLICommandHelper cmdHelper = new CLICommandHelper();
        assertThat(cmdHelper.computeUserFromHost(host).getUsername().matches(CLOUD_USERNAME));
    }

    @Test
    public void testReturnAdminUser() throws IOException {
        Host host = new Host();
        host.addUser(ROOT_USERNAME, ROOT_PASSWORD, UserType.ADMIN);
        CLICommandHelper cmdHelper = new CLICommandHelper();
        assertThat(cmdHelper.computeUserFromHost(host).getUsername().matches(ROOT_USERNAME));
    }

    @Test
    public void testOperUserReturnedWhenTwoUsersAdded() throws IOException {
        Host host = new Host();
        host.addUser(ROOT_USERNAME, ROOT_PASSWORD, UserType.ADMIN);
        host.addUser(CLOUD_USERNAME, CLOUD_PASSWORD, UserType.OPER);
        CLICommandHelper cmdHelper = new CLICommandHelper();
        assertThat(cmdHelper.computeUserFromHost(host).getUsername().matches(CLOUD_USERNAME));
    }

    @Test
    public void testCreateBlankOperatorUserIfUserNotDefinedForHost() throws IOException {
        Host host = new Host();
        CLICommandHelper cmdHelper = new CLICommandHelper();
        assertThat(cmdHelper.computeUserFromHost(host).getUsername().matches(""));
        assertThat(cmdHelper.computeUserFromHost(host).getPassword().matches(""));
        assertThat(cmdHelper.computeUserFromHost(host).getType().equals(UserType.OPER));
    }

    @Test
    public void testShouldReturnSpecifiedTypeIfNoOperOrAdminUser() throws IOException {
        Host host = new Host();
        host.addUser("test_user", "test_user_pass", UserType.NW_OPE);
        CLICommandHelper cmdHelper = new CLICommandHelper();
        assertThat(cmdHelper.computeUserFromHost(host).getType().equals(UserType.NW_OPE));
        assertThat(cmdHelper.computeUserFromHost(host).getUsername().matches("test_user"));
        assertThat(cmdHelper.computeUserFromHost(host).getPassword().matches("test_user_pass"));
    }
}
