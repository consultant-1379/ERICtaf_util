package com.ericsson.cifwk.taf.scenario.generated.out;

import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioTest;
import org.junit.Test;

import javax.annotation.Generated;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runnable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;

@Generated(
    value = "com.ericsson.cifwk.taf.scenario.generated.generator.Generator",
    date = "Wed Jun 15 09:10:31 BST 2016",
    comments = "Test to make sure that before/after steps and syncPoints will not hang the execution"
)
public final class BeforeAfterAndSyncPointTest3 extends ScenarioTest {
  @Test(
      timeout = 10000
  )
  public void scenarioTest901() {
    try {
      TestScenario scenario = scenario("scenario901")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .beforeFlow(pushToStack("before2599"))
                .afterFlow(pushToStack("after2600"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .addTestStep(runnable(pushToStack("step2601"))))
                .addTestStep(runnable(pushToStack("step2602"))))
            .addTestStep(runnable(pushToStack("step901")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest902() {
    try {
      TestScenario scenario = scenario("scenario902")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2603"))
                      .afterFlow(pushToStack("after2604"))
                      .addTestStep(runnable(pushToStack("step2605"))))
                .beforeFlow(pushToStack("before2606"))
                .afterFlow(pushToStack("after2607"))
                .addTestStep(runnable(pushToStack("step2608"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step902")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest903() {
    try {
      TestScenario scenario = scenario("scenario903")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2609"))
                      .afterFlow(pushToStack("after2610"))
                      .addTestStep(runnable(pushToStack("step2611"))))
                .beforeFlow(pushToStack("before2612"))
                .afterFlow(pushToStack("after2613"))
                .addTestStep(runnable(pushToStack("step2614"))))
            .addTestStep(runnable(pushToStack("step903")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest904() {
    try {
      TestScenario scenario = scenario("scenario904")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .beforeFlow(pushToStack("before2615"))
                .afterFlow(pushToStack("after2616"))
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2617"))
                      .afterFlow(pushToStack("after2618"))
                      .addTestStep(runnable(pushToStack("step2619"))))
                .addTestStep(runnable(pushToStack("step2620"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step904")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest905() {
    try {
      TestScenario scenario = scenario("scenario905")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .beforeFlow(pushToStack("before2621"))
                .afterFlow(pushToStack("after2622"))
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2623"))
                      .afterFlow(pushToStack("after2624"))
                      .addTestStep(runnable(pushToStack("step2625"))))
                .addTestStep(runnable(pushToStack("step2626"))))
            .addTestStep(runnable(pushToStack("step905")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest906() {
    try {
      TestScenario scenario = scenario("scenario906")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2627"))))
                .beforeFlow(pushToStack("before2628"))
                .afterFlow(pushToStack("after2629"))
                .addTestStep(runnable(pushToStack("step2630"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step906")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest907() {
    try {
      TestScenario scenario = scenario("scenario907")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2631"))))
                .beforeFlow(pushToStack("before2632"))
                .afterFlow(pushToStack("after2633"))
                .addTestStep(runnable(pushToStack("step2634"))))
            .addTestStep(runnable(pushToStack("step907")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest908() {
    try {
      TestScenario scenario = scenario("scenario908")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .beforeFlow(pushToStack("before2635"))
                .afterFlow(pushToStack("after2636"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2637"))))
                .addTestStep(runnable(pushToStack("step2638"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step908")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest909() {
    try {
      TestScenario scenario = scenario("scenario909")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .beforeFlow(pushToStack("before2639"))
                .afterFlow(pushToStack("after2640"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2641"))))
                .addTestStep(runnable(pushToStack("step2642"))))
            .addTestStep(runnable(pushToStack("step909")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest910() {
    try {
      TestScenario scenario = scenario("scenario910")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2643"))))
                .beforeFlow(pushToStack("before2644"))
                .afterFlow(pushToStack("after2645"))
                .addTestStep(runnable(pushToStack("step2646"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step910")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest911() {
    try {
      TestScenario scenario = scenario("scenario911")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2647"))))
                .beforeFlow(pushToStack("before2648"))
                .afterFlow(pushToStack("after2649"))
                .addTestStep(runnable(pushToStack("step2650"))))
            .addTestStep(runnable(pushToStack("step911")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest912() {
    try {
      TestScenario scenario = scenario("scenario912")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .beforeFlow(pushToStack("before2651"))
                .afterFlow(pushToStack("after2652"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2653"))))
                .addTestStep(runnable(pushToStack("step2654"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step912")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest913() {
    try {
      TestScenario scenario = scenario("scenario913")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .beforeFlow(pushToStack("before2655"))
                .afterFlow(pushToStack("after2656"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2657"))))
                .addTestStep(runnable(pushToStack("step2658"))))
            .addTestStep(runnable(pushToStack("step913")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest914() {
    try {
      TestScenario scenario = scenario("scenario914")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2659"))))
                .beforeFlow(pushToStack("before2660"))
                .afterFlow(pushToStack("after2661"))
                .addTestStep(runnable(pushToStack("step2662"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step914")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest915() {
    try {
      TestScenario scenario = scenario("scenario915")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2663"))))
                .beforeFlow(pushToStack("before2664"))
                .afterFlow(pushToStack("after2665"))
                .addTestStep(runnable(pushToStack("step2666"))))
            .addTestStep(runnable(pushToStack("step915")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest916() {
    try {
      TestScenario scenario = scenario("scenario916")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .beforeFlow(pushToStack("before2667"))
                .afterFlow(pushToStack("after2668"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2669"))))
                .addTestStep(runnable(pushToStack("step2670"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step916")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest917() {
    try {
      TestScenario scenario = scenario("scenario917")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .beforeFlow(pushToStack("before2671"))
                .afterFlow(pushToStack("after2672"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2673"))))
                .addTestStep(runnable(pushToStack("step2674"))))
            .addTestStep(runnable(pushToStack("step917")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest918() {
    try {
      TestScenario scenario = scenario("scenario918")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .addTestStep(annotatedMethod(this, "STEP_EXCEPTION_PRODUCER"))
                      .addTestStep(runnable(pushToStack("step2675"))))
                .withDataSources(dataSource("global"))
                .addTestStep(runnable(pushToStack("step2676"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step918")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest919() {
    try {
      TestScenario scenario = scenario("scenario919")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .addTestStep(annotatedMethod(this, "STEP_EXCEPTION_PRODUCER"))
                      .addTestStep(runnable(pushToStack("step2677"))))
                .withDataSources(dataSource("global"))
                .addTestStep(runnable(pushToStack("step2678"))))
            .addTestStep(runnable(pushToStack("step919")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest920() {
    try {
      TestScenario scenario = scenario("scenario920")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withDataSources(dataSource("global"))
                  .split(
                    flow("subSubFlow")
                      .addTestStep(annotatedMethod(this, "STEP_EXCEPTION_PRODUCER"))
                      .addTestStep(runnable(pushToStack("step2679"))))
                .addTestStep(runnable(pushToStack("step2680"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step920")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest921() {
    try {
      TestScenario scenario = scenario("scenario921")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withDataSources(dataSource("global"))
                  .split(
                    flow("subSubFlow")
                      .addTestStep(annotatedMethod(this, "STEP_EXCEPTION_PRODUCER"))
                      .addTestStep(runnable(pushToStack("step2681"))))
                .addTestStep(runnable(pushToStack("step2682"))))
            .addTestStep(runnable(pushToStack("step921")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest922() {
    try {
      TestScenario scenario = scenario("scenario922")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .addTestStep(runnable(pushToStack("step2683"))))
                .withDataSources(dataSource("global"))
                .addTestStep(runnable(pushToStack("step2684"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step922")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest923() {
    try {
      TestScenario scenario = scenario("scenario923")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .addTestStep(runnable(pushToStack("step2685"))))
                .withDataSources(dataSource("global"))
                .addTestStep(runnable(pushToStack("step2686"))))
            .addTestStep(runnable(pushToStack("step923")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest924() {
    try {
      TestScenario scenario = scenario("scenario924")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withDataSources(dataSource("global"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .addTestStep(runnable(pushToStack("step2687"))))
                .addTestStep(runnable(pushToStack("step2688"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step924")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest925() {
    try {
      TestScenario scenario = scenario("scenario925")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withDataSources(dataSource("global"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .addTestStep(runnable(pushToStack("step2689"))))
                .addTestStep(runnable(pushToStack("step2690"))))
            .addTestStep(runnable(pushToStack("step925")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest926() {
    try {
      TestScenario scenario = scenario("scenario926")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2691"))
                      .afterFlow(pushToStack("after2692"))
                      .addTestStep(runnable(pushToStack("step2693"))))
                .withDataSources(dataSource("global"))
                .addTestStep(runnable(pushToStack("step2694"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step926")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest927() {
    try {
      TestScenario scenario = scenario("scenario927")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2695"))
                      .afterFlow(pushToStack("after2696"))
                      .addTestStep(runnable(pushToStack("step2697"))))
                .withDataSources(dataSource("global"))
                .addTestStep(runnable(pushToStack("step2698"))))
            .addTestStep(runnable(pushToStack("step927")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest928() {
    try {
      TestScenario scenario = scenario("scenario928")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withDataSources(dataSource("global"))
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2699"))
                      .afterFlow(pushToStack("after2700"))
                      .addTestStep(runnable(pushToStack("step2701"))))
                .addTestStep(runnable(pushToStack("step2702"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step928")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest929() {
    try {
      TestScenario scenario = scenario("scenario929")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withDataSources(dataSource("global"))
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2703"))
                      .afterFlow(pushToStack("after2704"))
                      .addTestStep(runnable(pushToStack("step2705"))))
                .addTestStep(runnable(pushToStack("step2706"))))
            .addTestStep(runnable(pushToStack("step929")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest930() {
    try {
      TestScenario scenario = scenario("scenario930")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2707"))))
                .withDataSources(dataSource("global"))
                .addTestStep(runnable(pushToStack("step2708"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step930")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest931() {
    try {
      TestScenario scenario = scenario("scenario931")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2709"))))
                .withDataSources(dataSource("global"))
                .addTestStep(runnable(pushToStack("step2710"))))
            .addTestStep(runnable(pushToStack("step931")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest932() {
    try {
      TestScenario scenario = scenario("scenario932")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withDataSources(dataSource("global"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2711"))))
                .addTestStep(runnable(pushToStack("step2712"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step932")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest933() {
    try {
      TestScenario scenario = scenario("scenario933")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withDataSources(dataSource("global"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2713"))))
                .addTestStep(runnable(pushToStack("step2714"))))
            .addTestStep(runnable(pushToStack("step933")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest934() {
    try {
      TestScenario scenario = scenario("scenario934")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2715"))))
                .withDataSources(dataSource("global"))
                .addTestStep(runnable(pushToStack("step2716"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step934")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest935() {
    try {
      TestScenario scenario = scenario("scenario935")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2717"))))
                .withDataSources(dataSource("global"))
                .addTestStep(runnable(pushToStack("step2718"))))
            .addTestStep(runnable(pushToStack("step935")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest936() {
    try {
      TestScenario scenario = scenario("scenario936")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withDataSources(dataSource("global"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2719"))))
                .addTestStep(runnable(pushToStack("step2720"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step936")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest937() {
    try {
      TestScenario scenario = scenario("scenario937")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withDataSources(dataSource("global"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2721"))))
                .addTestStep(runnable(pushToStack("step2722"))))
            .addTestStep(runnable(pushToStack("step937")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest938() {
    try {
      TestScenario scenario = scenario("scenario938")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2723"))))
                .withDataSources(dataSource("global"))
                .addTestStep(runnable(pushToStack("step2724"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step938")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest939() {
    try {
      TestScenario scenario = scenario("scenario939")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2725"))))
                .withDataSources(dataSource("global"))
                .addTestStep(runnable(pushToStack("step2726"))))
            .addTestStep(runnable(pushToStack("step939")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest940() {
    try {
      TestScenario scenario = scenario("scenario940")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withDataSources(dataSource("global"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2727"))))
                .addTestStep(runnable(pushToStack("step2728"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step940")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest941() {
    try {
      TestScenario scenario = scenario("scenario941")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withDataSources(dataSource("global"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2729"))))
                .addTestStep(runnable(pushToStack("step2730"))))
            .addTestStep(runnable(pushToStack("step941")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest942() {
    try {
      TestScenario scenario = scenario("scenario942")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .addTestStep(annotatedMethod(this, "STEP_EXCEPTION_PRODUCER"))
                      .addTestStep(runnable(pushToStack("step2731"))))
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2732"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step942")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest943() {
    try {
      TestScenario scenario = scenario("scenario943")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .addTestStep(annotatedMethod(this, "STEP_EXCEPTION_PRODUCER"))
                      .addTestStep(runnable(pushToStack("step2733"))))
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2734"))))
            .addTestStep(runnable(pushToStack("step943")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest944() {
    try {
      TestScenario scenario = scenario("scenario944")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .addTestStep(annotatedMethod(this, "STEP_EXCEPTION_PRODUCER"))
                      .addTestStep(runnable(pushToStack("step2735"))))
                .addTestStep(runnable(pushToStack("step2736"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step944")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest945() {
    try {
      TestScenario scenario = scenario("scenario945")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .addTestStep(annotatedMethod(this, "STEP_EXCEPTION_PRODUCER"))
                      .addTestStep(runnable(pushToStack("step2737"))))
                .addTestStep(runnable(pushToStack("step2738"))))
            .addTestStep(runnable(pushToStack("step945")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest946() {
    try {
      TestScenario scenario = scenario("scenario946")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .addTestStep(runnable(pushToStack("step2739"))))
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2740"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step946")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest947() {
    try {
      TestScenario scenario = scenario("scenario947")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .addTestStep(runnable(pushToStack("step2741"))))
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2742"))))
            .addTestStep(runnable(pushToStack("step947")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest948() {
    try {
      TestScenario scenario = scenario("scenario948")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .addTestStep(runnable(pushToStack("step2743"))))
                .addTestStep(runnable(pushToStack("step2744"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step948")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest949() {
    try {
      TestScenario scenario = scenario("scenario949")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .addTestStep(runnable(pushToStack("step2745"))))
                .addTestStep(runnable(pushToStack("step2746"))))
            .addTestStep(runnable(pushToStack("step949")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest950() {
    try {
      TestScenario scenario = scenario("scenario950")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2747"))
                      .afterFlow(pushToStack("after2748"))
                      .addTestStep(runnable(pushToStack("step2749"))))
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2750"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step950")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest951() {
    try {
      TestScenario scenario = scenario("scenario951")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2751"))
                      .afterFlow(pushToStack("after2752"))
                      .addTestStep(runnable(pushToStack("step2753"))))
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2754"))))
            .addTestStep(runnable(pushToStack("step951")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest952() {
    try {
      TestScenario scenario = scenario("scenario952")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2755"))
                      .afterFlow(pushToStack("after2756"))
                      .addTestStep(runnable(pushToStack("step2757"))))
                .addTestStep(runnable(pushToStack("step2758"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step952")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest953() {
    try {
      TestScenario scenario = scenario("scenario953")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2759"))
                      .afterFlow(pushToStack("after2760"))
                      .addTestStep(runnable(pushToStack("step2761"))))
                .addTestStep(runnable(pushToStack("step2762"))))
            .addTestStep(runnable(pushToStack("step953")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest954() {
    try {
      TestScenario scenario = scenario("scenario954")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2763"))))
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2764"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step954")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest955() {
    try {
      TestScenario scenario = scenario("scenario955")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2765"))))
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2766"))))
            .addTestStep(runnable(pushToStack("step955")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest956() {
    try {
      TestScenario scenario = scenario("scenario956")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2767"))))
                .addTestStep(runnable(pushToStack("step2768"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step956")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest957() {
    try {
      TestScenario scenario = scenario("scenario957")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2769"))))
                .addTestStep(runnable(pushToStack("step2770"))))
            .addTestStep(runnable(pushToStack("step957")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest958() {
    try {
      TestScenario scenario = scenario("scenario958")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2771"))))
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2772"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step958")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest959() {
    try {
      TestScenario scenario = scenario("scenario959")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2773"))))
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2774"))))
            .addTestStep(runnable(pushToStack("step959")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest960() {
    try {
      TestScenario scenario = scenario("scenario960")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2775"))))
                .addTestStep(runnable(pushToStack("step2776"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step960")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest961() {
    try {
      TestScenario scenario = scenario("scenario961")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2777"))))
                .addTestStep(runnable(pushToStack("step2778"))))
            .addTestStep(runnable(pushToStack("step961")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest962() {
    try {
      TestScenario scenario = scenario("scenario962")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2779"))))
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2780"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step962")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest963() {
    try {
      TestScenario scenario = scenario("scenario963")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2781"))))
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2782"))))
            .addTestStep(runnable(pushToStack("step963")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest964() {
    try {
      TestScenario scenario = scenario("scenario964")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2783"))))
                .addTestStep(runnable(pushToStack("step2784"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step964")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest965() {
    try {
      TestScenario scenario = scenario("scenario965")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2785"))))
                .addTestStep(runnable(pushToStack("step2786"))))
            .addTestStep(runnable(pushToStack("step965")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest966() {
    try {
      TestScenario scenario = scenario("scenario966")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .addTestStep(annotatedMethod(this, "STEP_EXCEPTION_PRODUCER"))
                      .addTestStep(runnable(pushToStack("step2787"))))
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2788"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step966")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest967() {
    try {
      TestScenario scenario = scenario("scenario967")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .addTestStep(annotatedMethod(this, "STEP_EXCEPTION_PRODUCER"))
                      .addTestStep(runnable(pushToStack("step2789"))))
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2790"))))
            .addTestStep(runnable(pushToStack("step967")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest968() {
    try {
      TestScenario scenario = scenario("scenario968")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .addTestStep(annotatedMethod(this, "STEP_EXCEPTION_PRODUCER"))
                      .addTestStep(runnable(pushToStack("step2791"))))
                .addTestStep(runnable(pushToStack("step2792"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step968")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest969() {
    try {
      TestScenario scenario = scenario("scenario969")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .addTestStep(annotatedMethod(this, "STEP_EXCEPTION_PRODUCER"))
                      .addTestStep(runnable(pushToStack("step2793"))))
                .addTestStep(runnable(pushToStack("step2794"))))
            .addTestStep(runnable(pushToStack("step969")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest970() {
    try {
      TestScenario scenario = scenario("scenario970")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .addTestStep(runnable(pushToStack("step2795"))))
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2796"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step970")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest971() {
    try {
      TestScenario scenario = scenario("scenario971")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .addTestStep(runnable(pushToStack("step2797"))))
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2798"))))
            .addTestStep(runnable(pushToStack("step971")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest972() {
    try {
      TestScenario scenario = scenario("scenario972")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .addTestStep(runnable(pushToStack("step2799"))))
                .addTestStep(runnable(pushToStack("step2800"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step972")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest973() {
    try {
      TestScenario scenario = scenario("scenario973")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .addTestStep(runnable(pushToStack("step2801"))))
                .addTestStep(runnable(pushToStack("step2802"))))
            .addTestStep(runnable(pushToStack("step973")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest974() {
    try {
      TestScenario scenario = scenario("scenario974")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2803"))
                      .afterFlow(pushToStack("after2804"))
                      .addTestStep(runnable(pushToStack("step2805"))))
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2806"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step974")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest975() {
    try {
      TestScenario scenario = scenario("scenario975")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2807"))
                      .afterFlow(pushToStack("after2808"))
                      .addTestStep(runnable(pushToStack("step2809"))))
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2810"))))
            .addTestStep(runnable(pushToStack("step975")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest976() {
    try {
      TestScenario scenario = scenario("scenario976")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2811"))
                      .afterFlow(pushToStack("after2812"))
                      .addTestStep(runnable(pushToStack("step2813"))))
                .addTestStep(runnable(pushToStack("step2814"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step976")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest977() {
    try {
      TestScenario scenario = scenario("scenario977")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .beforeFlow(pushToStack("before2815"))
                      .afterFlow(pushToStack("after2816"))
                      .addTestStep(runnable(pushToStack("step2817"))))
                .addTestStep(runnable(pushToStack("step2818"))))
            .addTestStep(runnable(pushToStack("step977")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest978() {
    try {
      TestScenario scenario = scenario("scenario978")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2819"))))
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2820"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step978")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest979() {
    try {
      TestScenario scenario = scenario("scenario979")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2821"))))
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2822"))))
            .addTestStep(runnable(pushToStack("step979")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest980() {
    try {
      TestScenario scenario = scenario("scenario980")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2823"))))
                .addTestStep(runnable(pushToStack("step2824"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step980")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest981() {
    try {
      TestScenario scenario = scenario("scenario981")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("global"))
                      .addTestStep(runnable(pushToStack("step2825"))))
                .addTestStep(runnable(pushToStack("step2826"))))
            .addTestStep(runnable(pushToStack("step981")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest982() {
    try {
      TestScenario scenario = scenario("scenario982")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2827"))))
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2828"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step982")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest983() {
    try {
      TestScenario scenario = scenario("scenario983")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2829"))))
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2830"))))
            .addTestStep(runnable(pushToStack("step983")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest984() {
    try {
      TestScenario scenario = scenario("scenario984")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2831"))))
                .addTestStep(runnable(pushToStack("step2832"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step984")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest985() {
    try {
      TestScenario scenario = scenario("scenario985")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2833"))))
                .addTestStep(runnable(pushToStack("step2834"))))
            .addTestStep(runnable(pushToStack("step985")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest986() {
    try {
      TestScenario scenario = scenario("scenario986")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2835"))))
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2836"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step986")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest987() {
    try {
      TestScenario scenario = scenario("scenario987")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2837"))))
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                .addTestStep(runnable(pushToStack("step2838"))))
            .addTestStep(runnable(pushToStack("step987")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest988() {
    try {
      TestScenario scenario = scenario("scenario988")
        .addFlow(
          flow("mainFlow")
            .split(
              flow("subFlow")
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2839"))))
                .addTestStep(runnable(pushToStack("step2840"))))
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .addTestStep(runnable(pushToStack("step988")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }

  @Test(
      timeout = 10000
  )
  public void scenarioTest989() {
    try {
      TestScenario scenario = scenario("scenario989")
        .addFlow(
          flow("mainFlow")
          .withVusers(3)
          .withDataSources(dataSource("shared"))
            .split(
              flow("subFlow")
                .withVusers(3)
                .withDataSources(dataSource("shared"))
                  .split(
                    flow("subSubFlow")
                      .withVusers(3)
                      .withDataSources(dataSource("shared"))
                      .addTestStep(runnable(pushToStack("step2841"))))
                .addTestStep(runnable(pushToStack("step2842"))))
            .addTestStep(runnable(pushToStack("step989")))).build();
      runner.start(scenario);
    }
    catch (ScenarioTest.VerySpecialException e) {
    }
  }
}
