package com.ericsson.cifwk.taf.configuration;

import com.ericsson.cifwk.taf.itest.EmbeddedJetty;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class HttpConfiguration_ITest {

    static final Integer HTTPS_PORT = 55_443;
    public static final String TAF_HTTP_CONFIG_URL = "taf.http_config.url";
    EmbeddedJetty jetty;

    String config_hosts = "";
    String config_properties = "";
    String secondProps = "";

    HttpServlet servlet = new HttpServlet() {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            PrintWriter out = resp.getWriter();
            String type = req.getParameter("type");
            resp.setContentType("text/plain;charset=UTF-8");
            if ("hosts".equalsIgnoreCase(type)) {
                resp.setContentType("application/json;charset=UTF-8");
                out.write(config_hosts == null ? "" : config_hosts);
            } else if ("properties".equalsIgnoreCase(type)) {
                resp.setContentType("text/plain;charset=UTF-8");
                out.write(config_properties == null ? "" : config_properties);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            out.flush();
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    };

    HttpServlet servlet1 = new HttpServlet() {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            PrintWriter out = resp.getWriter();
            String type = req.getParameter("type");
            resp.setContentType("text/plain;charset=UTF-8");
            if ("properties".equalsIgnoreCase(type)) {
                resp.setContentType("text/plain;charset=UTF-8");
                out.write(secondProps == null ? "" : secondProps);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            out.flush();
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    };

    @Before
    public void setUp() throws Exception {
        jetty = EmbeddedJetty.build()
                .withServlet(servlet, "/config")
                .withServlet(servlet1, "/second")
                .withHttpsSupport(HTTPS_PORT)
                .start();
    }

    @After
    public void tearDown() throws Exception {
        jetty.stop();
        System.clearProperty(TAF_HTTP_CONFIG_URL);
    }

    @Test
    public void shouldLoadConfiguration_by_HTTP() throws Exception {
        System.setProperty(TAF_HTTP_CONFIG_URL, "http://localhost:" + jetty.getPort() + "/config");
        config_properties = "http.config.properties=exist\n" +
                "http.config.properties.first=first\n" +
                "http.config.properties.last=last";
        config_hosts = "[\n" +
                "    {\n" +
                "        \"type\": \"UNKNOWN\",\n" +
                "        \"hostname\": \"http.host.properties\",\n" +
                "        \"ip\": \"exist\",\n" +
                "        \"ports\": {\"http\": " + jetty.getPort() + "}\n" +
                "    }\n" +
                "]";
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();
        assertThat(build.getString("http.config.properties"), is("exist"));
        assertThat(build.getString("host.http.host.properties.ip"), is("exist"));
    }

    @Test
    public void shouldLoadConfiguration_by_HTTP_From_2_Url() throws Exception {
        String url1 = "http://localhost:" + jetty.getPort() + "/config";
        String url2 = "http://localhost:" + jetty.getPort() + "/second";
        String url = url1 + "," + url2;
        System.setProperty(TAF_HTTP_CONFIG_URL, url);
        config_properties = "http.config.properties=exist\n" +
                "http.config.properties.first=first\n" +
                "http.config.properties.last=last";
        config_hosts = "[\n" +
                "    {\n" +
                "        \"type\": \"UNKNOWN\",\n" +
                "        \"hostname\": \"http.host.properties\",\n" +
                "        \"ip\": \"exist\",\n" +
                "        \"ports\": {\"http\": " + jetty.getPort() + "}\n" +
                "    }\n" +
                "]";
        secondProps ="http.second.test.myProp=secondProp";

        TafConfiguration build = new TafConfigurationProvider().getConfiguration();
        assertThat(build.getString("http.config.properties"), is("exist"));
        assertThat(build.getString("host.http.host.properties.ip"), is("exist"));
        assertThat(build.getString("http.second.test.myProp"), is("secondProp"));
    }

    @Test
    public void shouldLoadAdvancedHostConfiguration() {
        System.setProperty(TAF_HTTP_CONFIG_URL, "http://localhost:" + jetty.getPort() + "/config");

        config_hosts = "[{\n" +
                "        \"hostname\": \"trigger_message_bus\",\n" +
                "        \"ip\": \"atvts977.athtem.eei.ericsson.se\",\n" +
                "        \"type\": \"RABBITMQ\",\n" +
                "        \"ports\": {\n" +
                "            \"http\": 8080,\n" +
                "            \"amqp\": 5672\n" +
                "        },\n" +
                "        \"nodes\": [\n" +
                "            {\n" +
                "                \"hostname\": \"available_internally\",\n" +
                "                \"ip\": \"mb1\",\n" +
                "                \"type\": \"RABBITMQ\",\n" +
                "                \"users\": [\n" +
                "                    {\n" +
                "                        \"username\": \"guest\",\n" +
                "                        \"password\": \"r0k9xcMA2yA2YLhW7wW4qToMncp5oUE7Vi/++wPFesA=\",\n" +
                "                        \"type\": \"admin\"\n" +
                "                    }\n" +
                "                ],\n" +
                "                \"ports\": {\n" +
                "                    \"http\": 8080,\n" +
                "                    \"amqp\": 5672\n" +
                "                }\n" +
                "\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    {\n" +
                "        \"hostname\": \"reporting_host\",\n" +
                "        \"ip\": \"mb1\",\n" +
                "        \"type\": \"UNKNOWN\",\n" +
                "        \"ports\": {\n" +
                "            \"http\": 8081\n" +
                "        },\n" +
                "        \"users\": [\n" +
                "            {\n" +
                "                \"username\": \"root\",\n" +
                "                \"password\": \"shroot\",\n" +
                "                \"type\": \"oper\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }]";

        System.out.println(config_hosts);
        TafConfiguration config = new TafConfigurationProvider().getConfiguration();
        assertThat(config.getString("host.trigger_message_bus.ip"), is("atvts977.athtem.eei.ericsson.se"));
        assertThat(config.getProperty("host.trigger_message_bus.port.amqp", int.class), is(5672));
        assertThat(config.getProperty("host.trigger_message_bus.port.http", int.class), is(8080));
        assertThat(config.getString("host.trigger_message_bus.node.available_internally.type"), is("RABBITMQ"));
        assertThat(config.getString("host.trigger_message_bus.node.available_internally.ip"), is("mb1"));
        assertThat(config.getProperty("host.trigger_message_bus.node.available_internally.port.http", int.class), is(8080));
        assertThat(config.getProperty("host.trigger_message_bus.node.available_internally.port.amqp", int.class), is(5672));
        assertThat(config.getString("host.trigger_message_bus.node.available_internally.user.guest.pass"), is("r0k9xcMA2yA2YLhW7wW4qToMncp5oUE7Vi/++wPFesA="));
        assertThat(config.getString("host.trigger_message_bus.node.available_internally.user.guest.type"), is("admin"));
        assertThat(config.getString("host.reporting_host.type"), is("UNKNOWN"));
        assertThat(config.getString("host.reporting_host.ip"), is("mb1"));
        assertThat(config.getProperty("host.reporting_host.port.http", int.class), is(8081));
        assertThat(config.getString("host.reporting_host.user.root.pass"), is("shroot"));
        assertThat(config.getString("host.reporting_host.user.root.type"), is("oper"));
    }

    @Test
    public void shouldLoadIpv6HostConfiguration() throws IOException {
        System.setProperty(TAF_HTTP_CONFIG_URL, "https://localhost:" + HTTPS_PORT + "/config");

        config_hosts = Resources.toString(Resources.getResource("ipv6.json"), Charsets.UTF_8);
        TafConfiguration config = new TafConfigurationProvider().getConfiguration();

        assertThat(config.getString("host.svc1.ip"), is("141.137.210.212"));
        assertThat(config.getString("host.svc1.ipv6"), is("2001:db8::1"));
        assertThat(config.getString("host.svc1.node.fmserv_1.ip"), is("10.247.246.37"));
        assertThat(config.getString("host.svc1.node.fmserv_1.ipv6"), is("2607:f0d0:1002:51::4"));
        assertThat(config.getString("host.svc1.node.pmserv_1.ip"), is("10.247.246.15"));
        assertThat(config.getString("host.svc1.node.pmserv_1.ipv6"), is("fe80::200:f8ff:fe21:67cf"));
    }


    @Test
    public void shouldProcessWrongURL_wo_Exception() throws Exception {
        System.setProperty(TAF_HTTP_CONFIG_URL, "http://localhost:" + jetty.getPort() + "/wrong-path");
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();
        assertThat(build.getString("http.config.properties"), not(is("exist")));
        assertThat(build.getString("host.http.host.properties.ip"), not(is("exist")));
    }

    @Test
    public void shouldLoadConfiguration_by_HTTPS() throws Exception {
        System.setProperty(TAF_HTTP_CONFIG_URL, "https://localhost:" + HTTPS_PORT + "/config");
        config_properties = "https.config.properties=exist\n" +
                "https.config.properties.first=first\n" +
                "https.config.properties.last=last";
        config_hosts = "[\n" +
                "    {\n" +
                "        \"hostname\": \"https.host.properties\",\n" +
                "        \"ip\": \"exist\",\n" +
                "        \"type\": \"UNKNOWN\",\n" +
                "        \"ports\": {}\n" +
                "    }\n" +
                "]";
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();
        assertThat(build.getString("https.config.properties"), is("exist"));
        assertThat(build.getString("host.https.host.properties.ip"), is("exist"));
    }

    @Test
    public void shouldLoadConfiguration_for_KVM_Hosts() throws IOException {
        System.setProperty(TAF_HTTP_CONFIG_URL, "https://localhost:" + HTTPS_PORT + "/config");

        config_hosts = Resources.toString(Resources.getResource("kvm.json"), Charsets.UTF_8);
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();
        assertThat(build.getString("host.svc1.type"), is("svc1"));
        assertThat(build.getString("host.svc1.node.ieatrcxb3794-2.port.http"), is("80"));
        assertThat(build.getString("host.svc1.node.ieatrcxb3794-2.ip"), is("141.137.208.216"));
        assertThat(build.getString("host.svc1.node.null.ip"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.jms_1.ip"), is("192.168.35.24"));

    }

    @Test
    public void shouldGetPubicIP_PublicAndInternal_in_Interfaces() throws IOException {
        System.setProperty(TAF_HTTP_CONFIG_URL, "https://localhost:" + HTTPS_PORT + "/config");

        config_hosts = Resources.toString(Resources.getResource("BothPublicandInternalInterface.json"), Charsets.UTF_8);
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();

        assertThat(build.getString("host.svc1.node.secserv_1.type"), is("jboss"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.ssh"), is("22"));
        assertThat(build.getString("host.svc1.node.secserv_1.group"), is("secserv"));
        assertThat(build.getString("host.svc1.node.secserv_1.unit"), is("1"));
        assertThat(build.getString("host.svc1.node.secserv_1.user.root.pass"), is("passw0rd"));
        assertThat(build.getString("host.svc1.node.secserv_1.user.root.type"), is("admin"));
        assertThat(build.getString("host.svc1.node.secserv_1.ip"), is("192.168.0.129"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.jboss_management"), is("9999"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.jmx"), is("9999"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.rmi"), is("4447"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.http"), is("8080"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.ip"), is("10.247.246.125"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.jboss_management"), is("9999"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.jmx"), is("9999"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.rmi"), is("4447"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.http"), is("8080"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.ssh"), is("22"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.tunnel"), is("6"));
        assertThat(build.getString("host.svc1.node.secserv_1.storage.ip"), is("172.16.30.125"));
        assertThat(build.getString("host.svc1.node.secserv_1.storage.port.jboss_management"), is("2222"));
        assertThat(build.getString("host.svc1.node.secserv_1.storage.port.jmx"), is("2222"));
        assertThat(build.getString("host.svc1.node.secserv_1.storage.port.rmi"), is("3335"));
        assertThat(build.getString("host.svc1.node.secserv_1.storage.port.http"), is("8080"));
        assertThat(build.getString("host.svc1.node.secserv_1.storage.port.ssh"), is("22"));
        assertThat(build.getString("host.svc1.node.secserv_1.storage.tunnel"), is(nullValue()));
    }

    @Test
    public void shouldGetInternalIPasDefault_OnlyInternal_in_Interfaces() throws IOException {
        System.setProperty(TAF_HTTP_CONFIG_URL, "https://localhost:" + HTTPS_PORT + "/config");

        config_hosts = Resources.toString(Resources.getResource("OnlyInternalInterface.json"), Charsets.UTF_8);
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();

        assertThat(build.getString("host.svc1.node.secserv_1.type"), is("jboss"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.ssh"), is("22"));
        assertThat(build.getString("host.svc1.node.secserv_1.group"), is("secserv"));
        assertThat(build.getString("host.svc1.node.secserv_1.unit"), is("1"));
        assertThat(build.getString("host.svc1.node.secserv_1.user.root.pass"), is("passw0rd"));
        assertThat(build.getString("host.svc1.node.secserv_1.user.root.type"), is("admin"));
        assertThat(build.getString("host.svc1.node.secserv_1.ip"), is("10.247.246.125"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.jboss_management"), is("9999"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.jmx"), is("9999"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.rmi"), is("4447"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.http"), is("8080"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.ip"), is("10.247.246.125"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.jboss_management"), is("9999"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.jmx"), is("9999"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.rmi"), is("4447"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.http"), is("8080"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.ssh"), is("22"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.tunnel"), is("6"));

    }

    @Test
    public void shouldGetPublicIPasDefault_OnlyPublic_in_Interfaces() throws IOException {
        System.setProperty(TAF_HTTP_CONFIG_URL, "https://localhost:" + HTTPS_PORT + "/config");

        config_hosts = Resources.toString(Resources.getResource("OnlyPublicInterface.json"), Charsets.UTF_8);
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();

        assertThat(build.getString("host.svc1.node.secserv_1.type"), is("jboss"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.ssh"), is("22"));
        assertThat(build.getString("host.svc1.node.secserv_1.group"), is("secserv"));
        assertThat(build.getString("host.svc1.node.secserv_1.unit"), is("1"));
        assertThat(build.getString("host.svc1.node.secserv_1.user.root.pass"), is("passw0rd"));
        assertThat(build.getString("host.svc1.node.secserv_1.user.root.type"), is("admin"));
        assertThat(build.getString("host.svc1.node.secserv_1.ip"), is("192.168.0.129"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.jboss_management"), is("9999"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.jmx"), is("9999"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.rmi"), is("4447"));
        assertThat(build.getString("host.svc1.node.secserv_1.port.http"), is("8080"));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.ip"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.jboss_management"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.jmx"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.rmi"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.http"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.port.ssh"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.secserv_1.internal.tunnel"), is(nullValue()));

    }

    @Test
    public void shouldGetValuesForStorageInterfaceOnly() throws IOException {
        System.setProperty(TAF_HTTP_CONFIG_URL, "https://localhost:" + HTTPS_PORT + "/config");

        config_hosts = Resources.toString(Resources.getResource("StorageInterface.json"), Charsets.UTF_8);
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();

        assertThat(build.getString("host.svc1.node.fmserv_1.type"), is("jboss"));
        assertThat(build.getString("host.svc1.node.fmserv_1.port.ssh"), is("22"));
        assertThat(build.getString("host.svc1.node.fmserv_1.group"), is("fmserv"));
        assertThat(build.getString("host.svc1.node.fmserv_1.unit"), is("1"));
        assertThat(build.getString("host.svc1.node.fmserv_1.user.root.pass"), is("passw0rd"));
        assertThat(build.getString("host.svc1.node.fmserv_1.user.root.type"), is("admin"));
        assertThat(build.getString("host.svc1.node.fmserv_1.ip"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.fmserv_1.storage.ip"), is("172.16.30.128"));
        assertThat(build.getString("host.svc1.node.fmserv_1.storage.port.jboss_management"), is("9999"));
        assertThat(build.getString("host.svc1.node.fmserv_1.storage.port.jmx"), is("9999"));
        assertThat(build.getString("host.svc1.node.fmserv_1.storage.port.rmi"), is("4447"));
        assertThat(build.getString("host.svc1.node.fmserv_1.storage.port.http"), is("8080"));
        assertThat(build.getString("host.svc1.node.fmserv_1.storage.port.ssh"), is("22"));
        assertThat(build.getString("host.svc1.node.fmserv_1.internal.ip"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.fmserv_1.internal.port.jboss_management"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.fmserv_1.internal.port.jmx"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.fmserv_1.internal.port.rmi"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.fmserv_1.internal.port.http"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.fmserv_1.internal.port.ssh"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.fmserv_1.internal.tunnel"), is(nullValue()));
    }

    @Test
    public void shouldNotReturnStorageInterfaceValuesWhenNonExistantInJson() throws IOException {
        System.setProperty(TAF_HTTP_CONFIG_URL, "https://localhost:" + HTTPS_PORT + "/config");

        config_hosts = Resources.toString(Resources.getResource("StorageInterface.json"), Charsets.UTF_8);
        TafConfiguration build = new TafConfigurationProvider().getConfiguration();

        assertThat(build.getString("host.svc1.node.pmserv_1.type"), is("jboss"));
        assertThat(build.getString("host.svc1.node.pmserv_1.port.ssh"), is("22"));
        assertThat(build.getString("host.svc1.node.pmserv_1.group"), is("pmserv"));
        assertThat(build.getString("host.svc1.node.pmserv_1.unit"), is("1"));
        assertThat(build.getString("host.svc1.node.pmserv_1.user.root.pass"), is("passw0rd"));
        assertThat(build.getString("host.svc1.node.pmserv_1.user.root.type"), is("admin"));
        assertThat(build.getString("host.svc1.node.pmserv_1.ip"), is("10.32.54.68"));
        assertThat(build.getString("host.svc1.node.pmserv_1.storage.ip"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.pmserv_1.storage.port.jboss_management"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.pmserv_1.storage.port.jmx"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.pmserv_1.storage.port.rmi"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.pmserv_1.storage.port.http"), is(nullValue()));
        assertThat(build.getString("host.svc1.node.pmserv_1.storage.port.ssh"), is(nullValue()));
    }
}
