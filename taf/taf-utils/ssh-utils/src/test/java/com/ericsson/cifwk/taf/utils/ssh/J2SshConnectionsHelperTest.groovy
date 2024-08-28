package com.ericsson.cifwk.taf.utils.ssh

import groovy.util.logging.Log4j
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@Log4j
class J2SshConnectionsHelperTest {

    static J2SshConnectionsHelper cut

    String fIp = "127.0.0.1"
    String sIp = "atrcx1933"
    String fUser = "lciadm100"
    String sUser = "lciadm10"
    String fPort = "22"
    String sPort = "23"
    String fPass = "lciadm100"
    String sPass = "lciadm10"

    @Before
    void prepareConnections() {
        cut = new J2SshConnectionsHelper().with {
            searchCurrentConnections(fIp, fUser, fPort, fPass)
            searchCurrentConnections(fIp, fUser, fPort, sPass)
            searchCurrentConnections(fIp, sUser, fPort, sPass)
            searchCurrentConnections(sIp, fUser, "22", "lciadm1")
            searchCurrentConnections(sIp, sUser, "23", "lciadm10")
            return it
        }
    }

    @Test
    void verifyNewConnectionsAreAddedToConnectionsPool() {
        cut.with {
            Assert.assertEquals(5, currentConnections.size())
        }
    }

    @Test
    void verifyObjectsAreSearchedCorrectly() {
        def conn, conn2
        conn = cut.searchCurrentConnections(fIp, fUser, fPort, fPass)
        conn2 = cut.searchCurrentConnections(fIp, fUser, fPort, sPass)
        Assert.assertTrue(!conn.is(conn2))
    }

    @Test
    void verifyConnectionsCanBeFetchedFromThePool() {
        def conn, conn2
        conn = cut.searchCurrentConnections(fIp, fUser, fPort, fPass)
        conn2 = cut.searchCurrentConnections(fIp, fUser, fPort, fPass)
        Assert.assertTrue(conn.is(conn2))
    }

}