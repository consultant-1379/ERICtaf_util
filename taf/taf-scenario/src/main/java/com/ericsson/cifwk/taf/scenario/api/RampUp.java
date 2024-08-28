/*
 * COPYRIGHT Ericsson (c) 2016.
 *
 *  The copyright to the computer program(s) herein is the property of
 *  Ericsson Inc. The programs may be used and/or copied only with written
 *  permission from Ericsson Inc. or in accordance with the terms and
 *  conditions stipulated in the agreement/contract under which the
 *  program(s) have been supplied.
 */

package com.ericsson.cifwk.taf.scenario.api;

import static com.ericsson.cifwk.meta.API.Quality.Stable;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.TimeUnit;

import com.ericsson.cifwk.meta.API;

@API(Stable)
public class RampUp {
    /**
     * @return Provider for strategy when all vUsers start at once
     */
    public static StrategyProvider allAtOnce() {
        return new StrategyProvider() {
            @Override
            public Strategy provideFor(final FlowRunOptions flowRunOptions) {
                return new Strategy() {
                    @Override
                    public long nextVUserDelayDelta() {
                        return 0;
                    }
                };
            }
        };
    }

    /**
     * @return Provider for strategy when all vUsers are started during defined time period. For example if 5 vUsers should be
     * started during 10 seconds, new vUser will be started each 10/5 = 2 seconds
     */
    public static StrategyProvider during(final long rampUpTime, final TimeUnit unit) {
        return new StrategyProvider() {
            @Override
            public Strategy provideFor(final FlowRunOptions flowRunOptions) {
                final long delay = MILLISECONDS.convert(rampUpTime, unit) / flowRunOptions.getVUsers();
                checkArgument(flowRunOptions.getVUsers() != -1, "When using RampUp .withVUsers(int) should be defined for flow");
                checkArgument(delay > 0, "Period to Ramp Up " + flowRunOptions.getVUsers() + " vUsers is too short");

                return new Strategy() {
                    @Override
                    public long nextVUserDelayDelta() {
                        return delay;
                    }
                };
            }
        };
    }

    /**
     * @return Builder for strategy when defined count of vUsers is started every defined time period. For example if
     * `vUsers(5).every(10, SECONDS)`, every 10 seconds 5 vUsers will start.
     */
    public static VUserStepStrategyBuilder vUsers(int vUsers) {
        return new VUserStepStrategyBuilder(vUsers);
    }

    public static class VUserStepStrategyBuilder {
        private int vUsersPerStep;

        public VUserStepStrategyBuilder(int vUsersPerStep) {
            this.vUsersPerStep = vUsersPerStep;
        }

        public StrategyProvider every(final long timePeriod, final TimeUnit unit) {
            return new StrategyProvider() {
                @Override
                public Strategy provideFor(final FlowRunOptions flowRunOptions) {
                    checkArgument(flowRunOptions.getVUsers() != -1, "When using RampUp .withVUsers(int) should be defined for flow");
                    checkArgument(flowRunOptions.getVUsers() >= vUsersPerStep, "Unable to Ramp Up more vUsers " +
                            "than defined in .withVUsers(int)");

                    return new Strategy() {
                        int vUser = -1;
                        long delayDelta = MILLISECONDS.convert(timePeriod, unit);

                        @Override
                        public long nextVUserDelayDelta() {
                            vUser++;
                            if (vUser != 0 && vUser % vUsersPerStep == 0) {
                                return delayDelta;
                            } else {
                                return 0;
                            }
                        }
                    };
                }
            };
        }
    }

    public interface Strategy {
        long nextVUserDelayDelta();
    }

    public interface StrategyProvider {
        Strategy provideFor(FlowRunOptions flowRunOptions);
    }
}
