package com.ericsson.cifwk.taf.scenario.generated.generator;

import com.ericsson.cifwk.taf.scenario.impl.ScenarioTest;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.taf.scenario.generated.generator.FlowModifier.all;
import static com.ericsson.cifwk.taf.scenario.generated.generator.FlowModifier.split;
import static com.ericsson.cifwk.taf.scenario.generated.generator.FlowModifier.throwException;
import static com.ericsson.cifwk.taf.scenario.generated.generator.FlowModifier.withBeforeAndAfterStep;
import static com.ericsson.cifwk.taf.scenario.generated.generator.FlowModifier.withDataSources;
import static com.ericsson.cifwk.taf.scenario.generated.generator.FlowModifier.withSyncPoint;
import static com.ericsson.cifwk.taf.scenario.generated.generator.FlowModifier.withVUsers;
import static com.ericsson.cifwk.taf.scenario.generated.generator.ScenarioModifier.addFlow;
import static com.google.common.collect.Lists.newArrayList;

public class Generator {
    private static AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) throws IOException {
        Path path = Paths.get(args[0]);

        generateSyncPointTest(path);
        generateBeforeFlowTest(path);
    }

    private static void generateBeforeFlowTest(Path path) throws IOException {
        List<FlowModifier> flowModifiers = newArrayList(
                throwException(),
                withVUsers(3),
                withBeforeAndAfterStep(),
                withDataSources(ScenarioTest.GLOBAL_DATA_SOURCE),
                withDataSources(ScenarioTest.SHARED_DATA_SOURCE),
                all(withVUsers(3), withDataSources(ScenarioTest.SHARED_DATA_SOURCE)));



        ScenarioModifier.Builder builder = ScenarioModifier.builder(
                "BeforeAfterAndSyncPointTest",
                "Test to make sure that before/after steps and syncPoints will not hang the execution",
                path,
                Generator.class);

        for (FlowModifier flowModifier : flowModifiers) {
            for (FlowModifier subFlowModifier : flowModifiers) {
                for (FlowModifier subSubFlowModifier : flowModifiers) {
                    FlowModifier subSubFlow = split("subSubFlow", subSubFlowModifier);
                    for (FlowModifier[] subFlowModifiers : shuffle(subSubFlow, subFlowModifier)) {
                        FlowModifier subflow = split("subFlow", subFlowModifiers);
                        for (FlowModifier[] mainFlowModifiers : shuffle(subflow, flowModifier)) {
                            ScenarioModifier flow = addFlow("mainFlow", mainFlowModifiers);
                            builder.addScenarioTest("scenarioTest" + counter.incrementAndGet(), flow);
                        }
                    }
                }
            }
        }

        builder.build();
    }

    private static Iterable<FlowModifier[]> shuffle(FlowModifier... flowModifiers) {
        return Iterables.transform(Collections2.permutations(Lists.newArrayList(flowModifiers)),
                new Function<List<FlowModifier>, FlowModifier[]>() {
                    @Override
                    public FlowModifier[] apply(List<FlowModifier> input) {
                        return input.toArray(new FlowModifier[input.size()]);
                    }
                });
    }

    private static void generateSyncPointTest(Path path) {
        List<FlowModifier> flowModifiers = newArrayList(
                throwException(),
                withVUsers(3),
                withSyncPoint(),
                withDataSources(ScenarioTest.GLOBAL_DATA_SOURCE),
                withDataSources(ScenarioTest.SHARED_DATA_SOURCE));

        ScenarioModifier.Builder builder = ScenarioModifier.builder(
                "SyncPointTest",
                "Test to make sure that syncPoints will not hang the execution",
                path,
                Generator.class);

        for (FlowModifier flowModifier : flowModifiers) {
            for (FlowModifier subFlowModifier : flowModifiers) {
                for (FlowModifier subSubFlowModifier : flowModifiers) {
                    builder.addScenarioTest("scenarioTest" + counter.incrementAndGet(),
                            addFlow("flow",
                                    split("subflow",
                                            split("subSubFlow", subSubFlowModifier),
                                            subFlowModifier),
                                    flowModifier)
                    );
                }
            }
        }

        builder.build();
    }
}
