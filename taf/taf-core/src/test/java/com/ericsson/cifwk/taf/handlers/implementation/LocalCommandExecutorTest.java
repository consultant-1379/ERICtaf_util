package com.ericsson.cifwk.taf.handlers.implementation;

import org.junit.Test;
import static com.google.common.truth.Truth.assertThat;

public class LocalCommandExecutorTest {

    private LocalCommandExecutor executor = new LocalCommandExecutor();

    @Test
    public void shouldStoreCommandOutputCorrectly(){
        boolean result = executor.execute("echo hello");
        assertThat(result).isTrue();
        assertThat(executor.getStdOut()).isEqualTo("hello");
        assertThat(executor.getExitCode()).isEqualTo(0);
        assertThat(executor.getStdErr()).isEmpty();
    }

    @Test
    public void shouldProcessCommandWithArgsCorrectly(){
        boolean result = executor.execute("ls -artl");
        assertThat(result).isTrue();
        assertThat(executor.getStdOut()).contains("total");
        assertThat(executor.getExitCode()).isEqualTo(0);
        assertThat(executor.getStdErr()).isEmpty();
    }

    @Test
    public void shouldReturnFalseForInvalidCommand(){
        boolean result = executor.execute("eco hello");
        assertThat(result).isFalse();
    }

    @Test
    public void shouldOutputToStandardError(){
        boolean result = executor.execute("java");
        assertThat(result).isFalse();
        assertThat(executor.getExitCode()).isEqualTo(1);
        assertThat(executor.getStdErr()).startsWith("Usage:");
    }

    @Test
    public void verifySimpleExecute(){
        String result = executor.simplExec("java");
        assertThat(result).isEmpty();
        assertThat(executor.getExitCode()).isEqualTo(1);
        assertThat(executor.getStdErr()).startsWith("Usage:");
    }


}