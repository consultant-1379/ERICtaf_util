package com.ericsson.cifwk.taf.maven.plugin;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 *
 */
public class TafCleanMojoTest {

    @Rule
    public MojoRule rule = new MojoRule() {
        @Override
        protected void before() throws Throwable {
        }

        @Override
        protected void after() {
        }
    };

    @Ignore
    @Test
    public void shouldCleanDirectories() throws Exception {
        File pom = new File(this.getClass().getResource("/test/clean/project-config.xml").getFile());
        assertNotNull(pom);
        assertTrue(pom.exists());

        TafCleanMojo cleanMojo = (TafCleanMojo) rule.lookupMojo("clean", pom);
        assertNotNull(cleanMojo);
    }

}
