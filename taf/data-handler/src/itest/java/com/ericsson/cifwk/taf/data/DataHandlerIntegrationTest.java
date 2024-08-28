package com.ericsson.cifwk.taf.data;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 31.03.2016
 */
public class DataHandlerIntegrationTest {

    private static final String FM_SERV_HOST = "fmserv_1";
    private static final String SVC1_HOST =  "svc1";

    @Test
    public void testSetParentName(){
        final Host host = DataHandler.getHostByName(FM_SERV_HOST);
        final String parentHost = "parentHost";

        assertThat(host.getParentName(), equalTo(SVC1_HOST));
        host.setParentName(parentHost);
        assertThat(host.getParentName(), equalTo(parentHost));
        host.setParentName(SVC1_HOST);
        assertThat(host.getParentName(), equalTo(SVC1_HOST));
    }

}
