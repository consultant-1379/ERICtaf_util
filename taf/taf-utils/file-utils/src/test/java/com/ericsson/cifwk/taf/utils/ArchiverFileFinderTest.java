package com.ericsson.cifwk.taf.utils;

import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class ArchiverFileFinderTest {

    @Test(expected = IllegalStateException.class)
    public void stripFileNameToClassNameFailure() {
        ArchiveFileFinder.stripFileNameToClassName("target.classes.");
    }

    @Test
    public void stripFileNameToClassNameCheckValidPath() {
        assertThat(ArchiveFileFinder.stripFileNameToClassName("com.taf.path.myjar.jar"), Matchers.equalTo("com.taf.path.myjar.jar"));
    }

    @Test
    public void stripFileNameToClassNameExcludeTargetPrefix() {
        assertThat(ArchiveFileFinder.stripFileNameToClassName("target.classes.com.taf.path.myjar.war"), Matchers.equalTo("com.taf.path.myjar.war"));
    }

    @Test
    public void stripFileNameToClassNameWithoutFileExtension() {
        assertThat(ArchiveFileFinder.stripFileNameToClassName("com.taf.path.myjar"), Matchers.equalTo("com.taf.path.myjar"));
    }

    @Test
    public void stripFileNameToClassNameCheckFileExtensionWithoutPath() {
        assertThat(ArchiveFileFinder.stripFileNameToClassName("myrar.zip"), Matchers.equalTo("myrar.zip"));
    }

}
