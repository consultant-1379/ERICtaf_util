/*
 * COPYRIGHT Ericsson (c) 2015.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.ericsson.cifwk.taf.scenario.impl;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TestContext;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@API(Internal)
public class VUserManager {
    private final Map<Integer, TestContext> contextMap = new ConcurrentHashMap<>();
    private int vUser = 0;

    protected void disposeContext() {
        contextMap.clear();
        vUser = 0;
    }

    protected Map<Integer, TestContext> getContextMap() {
        return contextMap;
    }

    protected List<Integer> allocateVUsers(int vUsersCnt) {
        TestContext parentContext = ServiceRegistry.getTestContextProvider().get();
        List<Integer> vUsers = Lists.newArrayList();

        for (int i = 0; i < vUsersCnt; i++) {
            vUser++;
            TestContext context = parentContext.createContextForVUser(vUser);
            contextMap.put(vUser, context);
            vUsers.add(vUser);
        }

        return vUsers;
    }
}
