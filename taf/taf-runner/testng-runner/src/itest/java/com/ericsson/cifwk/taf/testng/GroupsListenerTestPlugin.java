package com.ericsson.cifwk.taf.testng;

import com.ericsson.cifwk.taf.spi.TafPlugin;

/**
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 13/05/2016
 */
public class GroupsListenerTestPlugin implements TafPlugin {

    @Override
    public void init() {
        CompositeTestNGListener.cleanUpMethodInterceptors();
        CompositeTestNGListener.addListener(new GroupsListener() {
            @Override
            String getGroups() {
                return CmdLineGroupsHolder.getGroups();
            }
        }, 999);
    }

    @Override
    public void shutdown() {
    }
}
