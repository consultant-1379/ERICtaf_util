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
import com.ericsson.cifwk.taf.scenario.impl.configuration.ScenarioConfigurationProvider;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@API(Internal)
//todo this needs to be merged with SyncPoint
public class SyncPoint2 {
    public static final String SYNC_POINT_TIMEOUT = "taf.scenario.sync.point.timeout";
    public static final int SYNC_POINT_DEFAULT_TIMEOUT = 30;

    private static final Logger logger = LoggerFactory.getLogger(SyncPoint.class);

    private final String name;
    private final Integer runnersCount;
    private final Phaser phaser;
    private final String graphDebugInfo;
    private final Set<String> checkedOutExecutions = new HashSet<>();

    public SyncPoint2(final String name, Integer runnersCount, String graphDebugInfo) {
        logger.debug("SyncPoint2 `{}` initializing with runners count {}", name, runnersCount);
        this.name = name;
        this.graphDebugInfo = graphDebugInfo;
        this.runnersCount = runnersCount;
        this.phaser = new Phaser(runnersCount) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                logger.debug("SyncPoint2 `{}` advancing", name);
                SyncPoint2.this.onAdvance();
                return false;
            }
        };
    }

    protected void onAdvance() {
    }

    public void checkIn(String executionId) {

        synchronized (phaser) {
            if (phaser.getUnarrivedParties() == 0) {
                //reset phaser: need to determine how much to register
                int relativesLeft = runnersCount;

                for (String checkedOutParent : checkedOutExecutions) {
                    checkedOutParent = executionId.substring(0, checkedOutParent.lastIndexOf("."));

                    if (executionId.startsWith(checkedOutParent)) {
                        relativesLeft--;
                    }
                }

                if (relativesLeft <= 0) {
                    //wait for self
                    relativesLeft = 1;
                }

                logger.debug("SyncPoint2 `{}` resetting: execution {}, checked out {}, relatives left {}", name, executionId, checkedOutExecutions, relativesLeft);
                phaser.bulkRegister(relativesLeft);
            }
            checkedOutExecutions.add(executionId);
        }

        logger.debug("SyncPoint2 `{}` check in: execution {}, left {}", name, executionId, phaser.getUnarrivedParties());
        arriveAndAwaitAdvance();

        logger.debug("SyncPoint2 `{}` continue: execution {}", name, executionId);
    }

    private void arriveAndAwaitAdvance() {
        try {
            int phase = phaser.arrive();
            phaser.awaitAdvanceInterruptibly(phase, getTimeout(), TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        } catch (TimeoutException e) {
            logger.error("Timeout {}m while waiting at Sync Point {}: phaser {}. If execution requires more time " +
                            "use -D{} property to set timeout in minutes.\n Graph {}"
                    , getTimeout(), name, phaser, SYNC_POINT_TIMEOUT, graphDebugInfo);
        }
    }

    public void cancelSelf(String executionId) {
        synchronized (phaser) {
            if (phaser.getUnarrivedParties() == 0) {

                logger.debug("SyncPoint2 `{}` all flows checked out: execution {}", name, executionId);
                checkedOutExecutions.add(executionId);
                return;
            }

            //if not waiting on checkOut
            if (checkedOutExecutions.add(executionId)) {
                logger.debug("SyncPoint2 `{}` cancel: execution {}", name, executionId);
                phaser.arriveAndDeregister();
            } else {
                logger.debug("SyncPoint2 `{}`: execution {} already checked out", name, executionId);
            }
        }

    }

    public void cancelParent(String executionId) {

        synchronized (phaser) {
            checkedOutExecutions.add(executionId);

            for (String checkedOutParent : checkedOutExecutions) {
                // Keep in mind that child always checks out before parent
                // So if at least child already checked out its not our case
                // We need to handle case when children has not started
                if (checkedOutParent.startsWith(executionId + ".")) {
                    logger.debug("SyncPoint2 `{}` child {} for parent {} already checked out", name, checkedOutParent, executionId);
                    return;
                }
            }

            logger.debug("SyncPoint2 `{}` cancel by parent: execution {}", name, executionId);
            phaser.arriveAndDeregister();
        }
    }

    public void fail(String executionId) {
        synchronized (phaser) {
            checkedOutExecutions.add(executionId);

            if (phaser.getUnarrivedParties() != 0 && phaser.getRegisteredParties() != 0) {
                phaser.arriveAndDeregister();
            }
        }
    }

    private Integer getTimeout() {
        return ScenarioConfigurationProvider.provide().getProperty(SYNC_POINT_TIMEOUT, SYNC_POINT_DEFAULT_TIMEOUT);
    }
}
