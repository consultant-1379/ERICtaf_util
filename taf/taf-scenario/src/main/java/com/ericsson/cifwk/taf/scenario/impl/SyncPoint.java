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
import com.ericsson.cifwk.taf.scenario.impl.configuration.ScenarioConfigurationProvider;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@API(Internal)
public class SyncPoint {
    public static final String SYNC_POINT_TIMEOUT = "taf.scenario.sync.point.timeout";
    public static final int SYNC_POINT_DEFAULT_TIMEOUT = 30;

    private static final Logger logger = LoggerFactory.getLogger(SyncPoint.class);

    private final String id;
    private final Phaser phaser;
    private final String graphDebugInfo;

    public SyncPoint(String id, int initialCount, String graphDebugInfo) {
        logger.debug("{}: initialized with count {}", id, initialCount);
        this.graphDebugInfo = graphDebugInfo;
        this.id = id;
        this.phaser = new Phaser(initialCount) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                return SyncPoint.this.onAdvance(phase, registeredParties);
            }
        };
    }

    protected boolean onAdvance(int phase, int registeredParties) {
        return registeredParties == 0;
    }

    public void await() {
        int vUser = ServiceRegistry.getTestContextProvider().get().getVUser();
        try {
            logger.debug("Sync Point {}: vUser {} awaiting; phaser {}", id, vUser, phaser);
            int phase = phaser.arrive();
            phaser.awaitAdvanceInterruptibly(phase, getTimeout(), TimeUnit.MINUTES);
            logger.debug("Sync Point {}: vUser {} continuing; phaser {}", id, vUser, phaser);
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        } catch (TimeoutException e) {
            /* This should never happen, and covered by tests in com.ericsson.cifwk.taf.scenario.impl.ScenarioSyncPointsTest
             If in some bizarre case it still happens we want full debug info */
            logger.error("Timeout {}m while waiting at Sync Point {}: vUser {}; phaser {}. If execution requires more time " +
                            "use -D{} property to set timeout in minutes.\n Graph {}"
                    , getTimeout(), id, vUser, phaser, SYNC_POINT_TIMEOUT, graphDebugInfo);
        }
    }

    public void stopWaiting() {
        logger.debug("{}: stopped waiting; phaser {}", id, phaser);
        phaser.arriveAndDeregister();
    }

    public void moveForward() {
        logger.debug("{}: arrival; phaser {}", id, phaser);
        phaser.arrive();
    }

    private Integer getTimeout() {
        return ScenarioConfigurationProvider.provide().getProperty(SYNC_POINT_TIMEOUT, SYNC_POINT_DEFAULT_TIMEOUT);
    }
}
