package com.ericsson.cifwk.taf.scenario.spi;

import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.taf.scenario.impl.ParallelInvocation;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowExecutionCancelledMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedAllIterationsMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.FlowStartedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.ScenarioStartedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.TestStepFinishedMessage;
import com.ericsson.cifwk.taf.scenario.impl.messages.TestStepStartedMessage;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.eventbus.Subscribe;

import java.util.Collections;

public class TestScenarioMessageListener implements ScenarioMessageListener {

    private static final ThreadLocal<Multiset<Class<? extends ScenarioMessage>>> messages  = new InheritableThreadLocal<Multiset<Class<? extends ScenarioMessage>>>() {
        @Override
        protected Multiset<Class<? extends ScenarioMessage>> initialValue() {
            return ConcurrentHashMultiset.create();
        }
    };

    @Subscribe
    public void onScenarioStartedMessage(ScenarioStartedMessage message) {
        messages.get().add(message.getClass());
        MessageHolder.put(message);
    }

    @Subscribe
    public void onScenarioFlowStarted(FlowStartedMessage.ScenarioFlow message) {
        messages.get().add(message.getClass());
        MessageHolder.put(message);
    }

    @Subscribe
    public void onUserFlowStarted(FlowStartedMessage.UserFlow message) {
        messages.get().add(message.getClass());
        MessageHolder.put(message);
    }

    @Subscribe
    public void onTestStepStartedMessage(TestStepStartedMessage message) {
        messages.get().add(message.getClass());
        MessageHolder.put(message);
    }

    @Subscribe
    public void onTestStepSuccess(TestStepFinishedMessage.Success message) {
        messages.get().add(message.getClass());
        MessageHolder.put(message);
    }

    @Subscribe
    public void onTestStepFailed(TestStepFinishedMessage.Failed message) {
        messages.get().add(message.getClass());
        MessageHolder.put(message);
    }

    @Subscribe
    public void onUserFlowSuccess(FlowFinishedMessage.UserFlowSuccess message) {
        messages.get().add(message.getClass());
        MessageHolder.put(message);
    }

    @Subscribe
    public void onUserFlowFailed(FlowFinishedMessage.UserFlowFailed message) {
        messages.get().add(message.getClass());
        MessageHolder.put(message);
    }

    @Subscribe
    public void onScenarioFlowSuccess(FlowFinishedMessage.ScenarioFlowSuccess message) {
        messages.get().add(message.getClass());
        MessageHolder.put(message);
    }

    @Subscribe
    public void onScenarioFlowFailed(FlowFinishedMessage.ScenarioFlowFailed message) {
        messages.get().add(message.getClass());
        MessageHolder.put(message);
    }

    @Subscribe
    public void onFlowExecutionCancelledMessage(FlowExecutionCancelledMessage message) {
        messages.get().add(message.getClass());
        MessageHolder.put(message);
    }

    @Subscribe
    public void onFlowFinishedAllIterationsMessage(FlowFinishedAllIterationsMessage message) {
        messages.get().add(message.getClass());
        MessageHolder.put(message);
    }

    @Subscribe
    public void onScenarioFinishedMessage(ScenarioFinishedMessage message) {
        messages.get().add(message.getClass());
        MessageHolder.put(message);
    }

    public static int count(Class<? extends ScenarioMessage> messageClass) {
        return messages.get().count(messageClass);
    }

    public static void reset() {
        messages.remove();
    }

    /**
     * Temporary structure to be used in tests.
     * Currently can't be used from tests since count() method required scenario message object instance.
     *
     * To be removed on being confirmed to be useless.
     */
    public static class MessageHolder {

        private static final String FLOW = "flow:";

        private static final String SCENARIO = "scenario:";

        private static final String INVOCATION = "invocation:";

        private static ListMultimap<String, Class<? extends ScenarioMessage>> messages
                = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, Class<? extends ScenarioMessage>>create());

        public static void put(ScenarioStartedMessage message) {
            put(message.getScenario(), message);
        }

        public static void put(ScenarioFinishedMessage message) {
            put(message.getScenario(), message);
        }

        public static void put(FlowFinishedMessage message) {
            put(message.getFlow(), message);
        }

        public static void put(FlowStartedMessage message) {
            put(message.getFlow(), message);
        }

        public static void put(TestStepStartedMessage message) {
            put(message.getFlow(), message);
        }

        public static void put(TestStepFinishedMessage message) {
            put(message.getFlow(), message);
        }

        public static void put(FlowFinishedAllIterationsMessage message) {
            put(message.getFlow(), message);
        }

        public static void put(FlowExecutionCancelledMessage message) {
            put(message.getInvocation(), message);
        }

        public static int count(ScenarioStartedMessage message) {
            return count(message.getScenario(), message);
        }

        public static int count(ScenarioFinishedMessage message) {
            return count(message.getScenario(), message);
        }

        public static int count(FlowFinishedMessage message) {
            return count(message.getFlow(), message);
        }

        public static int count(FlowStartedMessage message) {
            return count(message.getFlow(), message);
        }

        public static int count(TestStepStartedMessage message) {
            return count(message.getFlow(), message);
        }

        public static int count(TestStepFinishedMessage message) {
            return count(message.getFlow(), message);
        }

        public static int count(FlowFinishedAllIterationsMessage message) {
            return count(message.getFlow(), message);
        }

        public static int count(FlowExecutionCancelledMessage message) {
            return count(message.getInvocation(), message);
        }

        private static void put(TestScenario scenario, ScenarioMessage message) {
            messages.get(SCENARIO + scenario.getId()).add(message.getClass());
        }

        private static void put(TestStepFlow flow, ScenarioMessage message) {
            messages.get(FLOW + flow.getId()).add(message.getClass());
        }

        private static void put(ParallelInvocation invocation, ScenarioMessage message) {
            messages.get(INVOCATION + invocation.getId()).add(message.getClass());
        }

        private static int count(TestScenario scenario, ScenarioMessage message) {
            return Collections.frequency(messages.get(SCENARIO + scenario.getId()), message.getClass());
        }

        private static int count(TestStepFlow flow, ScenarioMessage message) {
            return Collections.frequency(messages.get(FLOW + flow.getId()), message.getClass());
        }

        private static int count(ParallelInvocation invocation, ScenarioMessage message) {
            return Collections.frequency(messages.get(INVOCATION + invocation.getId()), message.getClass());
        }

    }

}
