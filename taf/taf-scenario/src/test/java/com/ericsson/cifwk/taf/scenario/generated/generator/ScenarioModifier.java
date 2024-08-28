package com.ericsson.cifwk.taf.scenario.generated.generator;

import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.cifwk.taf.scenario.impl.ScenarioTest;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.junit.Test;

import javax.annotation.Generated;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ScenarioModifier implements Predicate<CodeBlock.Builder> {
    public static final int TESTS_PER_CLASS = 300;

    private static AtomicInteger counter = new AtomicInteger();


    public static ScenarioModifier addFlow(final String name, final FlowModifier... flowModifier) {
        return new ScenarioModifier() {
            @Override
            public boolean apply(CodeBlock.Builder builder) {
                builder.indent().add("\n.addFlow(");
                builder.indent().add("\nflow($S)", name);

                for (FlowModifier modifier : flowModifier) {
                    modifier.apply(builder);
                }

                builder.indent().add("\n.addTestStep(runnable(pushToStack($S)))", "step" + counter.incrementAndGet());

                builder.unindent().add(")").unindent().unindent();

                return false;
            }
        };
    }

    public static Builder builder(String name, String comment, Path path, Class<Generator> generatorClass) {
        return new Builder(name, comment, path, generatorClass);
    }

    public static class Builder {
        private static AtomicInteger testCounter = new AtomicInteger();
        private TypeSpec.Builder testClassBuilder;
        private Path path;
        private String name;
        private String comment;
        private Class<Generator> generatorClass;

        Builder(String name, String comment, Path path, Class<Generator> generatorClass) {
            this.path = path;
            testClassBuilder = newClassBuilder(name, comment, generatorClass);
        }

        private TypeSpec.Builder newClassBuilder(String name, String comment, Class<Generator> generatorClass) {
            this.name = name;
            this.comment = comment;
            this.generatorClass = generatorClass;
            return TypeSpec.classBuilder(name + testCounter.get() / TESTS_PER_CLASS)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(AnnotationSpec.builder(Generated.class)
                            .addMember("value", "$S", generatorClass.getCanonicalName())
                            .addMember("date", "$S", new Date())
                            .addMember("comments", "$S", comment)
                            .build())
                    .superclass(ScenarioTest.class);
        }

        public void addScenarioTest(String testName, ScenarioModifier... scenarioModifiers) {
            CodeBlock.Builder scenarioBuilder = CodeBlock.builder();

            scenarioBuilder.beginControlFlow("try");
            scenarioBuilder.add("$T scenario = scenario($S)", TestScenario.class, "scenario" + testCounter.incrementAndGet());

            for (ScenarioModifier modifier : scenarioModifiers) {
                modifier.apply(scenarioBuilder);
            }

            scenarioBuilder.addStatement(".build()");
            scenarioBuilder.addStatement("runner.start(scenario)");
            scenarioBuilder.endControlFlow().beginControlFlow("catch ($T e)", ScenarioTest.VerySpecialException.class).endControlFlow();

            testClassBuilder.addMethod(MethodSpec.methodBuilder(testName)
                    .addAnnotation(
                            AnnotationSpec.builder(Test.class)
                                    .addMember("timeout", "$L", 10_000)
                                    .build()
                    )
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addCode(scenarioBuilder.build())
                    .build());

            if (testCounter.get() % TESTS_PER_CLASS == 0) {
                build();
                testClassBuilder = newClassBuilder(name, comment, generatorClass);
            }
        }

        public void build() {
            try {
            JavaFile javaFile = JavaFile
                    .builder("com.ericsson.cifwk.taf.scenario.generated.out", testClassBuilder.build())
                    .addStaticImport(TestScenarios.class, "*")
                    .build();

                javaFile.writeTo(System.out);
                javaFile.writeTo(path);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
    }
}
