/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.handlers;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;

/**
 * Class to help with getting AS service over RMI client connection
 */
public class AsRmiHandler {

    private static Logger log = LoggerFactory.getLogger(AsRmiHandler.class);

    public static final String INITIAL_CONTEXT_FACTORY = "org.jboss.naming.remote.client.InitialContextFactory";

    private Context ctx;

    private Host jbossNode;

    private Object service;

    /**
     * Constructor requiring JBOSS node as argument. If JBOSS node has not RMI
     * port, error is thrown
     *
     * @param jbossNode
     * @throws Throwable
     */
    public AsRmiHandler(Host jbossNode) {
        this.jbossNode = jbossNode;
        String port = "";
        Map<Ports, String> ports;

        if (this.jbossNode != null) {
            ports = this.jbossNode.getPort();
            port = (String) ports.get(Ports.RMI);
        }
        if (port == null) {
            log.error("No RMI port defined for JBOSS node "
                    + jbossNode.getNodes() + ". Cannot make connection");
            throw new RuntimeException("RMI port undefined for host "
                    + jbossNode);
        }
    }

    /**
     * Set up the context for the JNDI lookup
     *
     * @return
     */
    public Context getContext() {
        log.debug("Setting up Context. I should only see this once (per thread) "
                + this);
        try {
            if (ctx == null) {
                log.debug("Set up the context for the JNDI lookup");
                Map<Object, Object> env = new HashMap<>();
                List<User> applicationRealUser = jbossNode
                        .getUsers(UserType.OPER);
                log.debug("applicationRealUser " + applicationRealUser);
                String userName;
                String pass;
                if (applicationRealUser == null) {
                    log.error("Cannot find application realm user");
                    userName = jbossNode.getUser();
                    pass = jbossNode.getPass();
                    log.warn("Using default user " + userName + " and " + pass);
                } else {
                        for (Iterator<User> iter = applicationRealUser.listIterator(); iter.hasNext(); ) {
                        User a = iter.next();
                        if (a.getUsername().equals("cloud-user")) {
                                log.debug("Removing cloud user from list. Temporary solution until new Jboss application user created in DMT");
                            iter.remove();
                        }
                    }
                    userName = applicationRealUser.get(0).getUsername();
                    pass = applicationRealUser.get(0).getPassword();
                }
                log.debug("userName" + userName);
                log.debug("pass" + pass);
                log.debug("ip" + jbossNode.getIp());
                String providerUrl = "remote://" + jbossNode.getIp() + ":"
                        + jbossNode.getPort().get(Ports.RMI).toString();
                env.put(Context.INITIAL_CONTEXT_FACTORY,
                        INITIAL_CONTEXT_FACTORY);
                env.put(Context.PROVIDER_URL, providerUrl);
                env.put(Context.SECURITY_PRINCIPAL, userName);
                env.put(Context.SECURITY_CREDENTIALS, pass);
                env.put("jboss.naming.client.ejb.context", true);
                ctx = new InitialContext(new Hashtable<>(env));
            }
        } catch (Exception e) {
            log.debug("Issue with creating InitialContext in AsRmiHandler " + e);
        }
        log.debug("Returning context " + ctx);
        return ctx;
    }


    /**
     * Get AS Service via JNDI lookup
     *
     * @param jndiString
     * @return
     * @throws NamingException
     */
    public Object getServiceViaJndiLookup(String jndiString)
            throws NamingException {
        try {
            service = getContext().lookup(jndiString);
        } catch (Exception e) {
            log.error("Problem using lookup: " + e);
            throw e;
        }
        log.debug("Found service " + service);
        return service;
    }

    /**
     * Get Versions of deployed services
     *
     * @param ServiceNamePattern
     * @return Sorted list of versions
     * @throws NamingException
     */
    public List<String> getServiceVersion(String serviceNamePattern)
            throws NamingException {
        ArrayList<String> versions = new ArrayList<>();
        NamingEnumeration<Binding> list = null;
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                list = getContext().listBindings("");
                break;
            } catch (NamingException e) {
                log.error("Problem listing bindings: " + e);
                try {
                    log.warn("Restarting Tunnel for RMI Port..RetryCount: "
                            + (retryCount + 1));
                    ctx = null;
                    jbossNode.reStartTunnel();
                } catch (Exception e1) {
                    log.error("Problem in restarting the tunnel " + e1);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e2) {
                        log.error("Problem in the wait" + e2);
                    }
                }
                retryCount++;
                if (retryCount == 3)
                    throw e;
            }
        }
        try {
            while (list.hasMore()) {
                String name = list.next().getName();
                if (name.contains(serviceNamePattern)) {
                    versions.add(name.substring(name.lastIndexOf('-') + 1));
                }
            }
        } catch (NamingException e) {
            log.error("Problem listing bindings: " + e);
        }
        Collections.sort(versions);
        return versions;
    }

    /**
     * Method to close the context
     *
     * @throws NamingException
     */
    public void close() {
        try {
            log.debug("Closing RMI context " + ctx);
            ctx.close();
        } catch (Exception e) {
            log.error("Problem using close", e);
        }
    }

}
