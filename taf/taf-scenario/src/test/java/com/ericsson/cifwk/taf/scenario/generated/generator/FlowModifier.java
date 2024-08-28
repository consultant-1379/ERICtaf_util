package com.ericsson.cifwk.taf.scenario.generated.generator;

import com.google.common.base.Predicate;
import com.squareup.javapoet.CodeBlock;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class FlowModifier implements Predicate<CodeBlock.Builder> {
    private static AtomicInteger counter = new AtomicInteger();

    public static FlowModifier all(final FlowModifier... flowModifiers) {
        return new FlowModifier() {
            @Override
            public boolean apply(CodeBlock.Builder input) {
                for (FlowModifier flowModifier : flowModifiers) {
                    flowModifier.apply(input);
                }

                return false;
            }
        };
    }

    public static FlowModifier all(final FlowModifier[] flowModifiersArray, final FlowModifier... flowModifiers) {
        return new FlowModifier() {
            @Override
            public boolean apply(CodeBlock.Builder input) {
                for (FlowModifier flowModifier : flowModifiersArray) {
                    flowModifier.apply(input);
                }

                for (FlowModifier flowModifier : flowModifiers) {
                    flowModifier.apply(input);
                }

                return false;
            }
        };
    }

    public static FlowModifier throwException() {
        return new FlowModifier() {
            @Override
            public boolean apply(CodeBlock.Builder builder) {
                builder.add("\n.addTestStep(annotatedMethod(this, $S))", "STEP_EXCEPTION_PRODUCER");

                return false;
            }
        };
    }

    public static FlowModifier withVUsers(final int vUsers) {
        return new FlowModifier() {
            @Override
            public boolean apply(CodeBlock.Builder builder) {
                builder.add("\n.withVusers($L)", vUsers);

                return false;
            }
        };
    }

    public static FlowModifier withDataSources(final String name) {
        return new FlowModifier() {
            @Override
            public boolean apply(CodeBlock.Builder builder) {
                builder.add("\n.withDataSources(dataSource($S))", name);
                return false;
            }
        };
    }

    public static FlowModifier split(final String name, final FlowModifier... flowModifiers) {
        return new FlowModifier() {
            @Override
            public boolean apply(CodeBlock.Builder builder) {
                builder.indent().add("\n.split(");
                builder.indent().add("\nflow($S)", name);

                builder.indent();
                for (FlowModifier modifier : flowModifiers) {
                    modifier.apply(builder);
                }
                builder.add("\n.addTestStep(runnable(pushToStack($S)))", "step" + counter.incrementAndGet());
                builder.unindent();

                builder.add(")").unindent().unindent();

                return false;
            }
        };
    }

    public static FlowModifier withSyncPoint() {
        return new FlowModifier() {
            @Override
            public boolean apply(CodeBlock.Builder builder) {
                builder.add("\n.syncPoint($S)", "hanger");

                return false;
            }
        };
    }

    public static FlowModifier withBeforeAndAfterStep() {
        return new FlowModifier() {
            @Override
            public boolean apply(CodeBlock.Builder builder) {
                builder.add("\n.beforeFlow(pushToStack($S))", "before" + counter.incrementAndGet());
                builder.add("\n.afterFlow(pushToStack($S))", "after" + counter.incrementAndGet());

                return false;
            }
        };
    }
}
