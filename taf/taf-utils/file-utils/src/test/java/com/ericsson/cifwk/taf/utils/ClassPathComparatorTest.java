package com.ericsson.cifwk.taf.utils;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertThat;

/**
 * @author Mihails Volkovs mihails.volkovs@ericsson.com
 *         Date: 19.04.2016
 */
public class ClassPathComparatorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void compare() throws IOException {
        File folder1 = folder.newFolder("temp1");
        File folder2 = folder.newFolder("temp2");

        File eric1 = new File(".m2/repository/com/ericsson/cifwk/taf-datasources/2.20.10/taf-datasources-2.20.10.jar");
        File eric2 = new File(".m2/repository/com/ericsson/cifwk/taf-datasources/2.20.20/taf-datasources-2.20.20.jar");

        File external1 = new File(".m2/repository/org/hibernate/hibernate-validator/5.1.2.Final/hibernate-validator-5.1.2.Final.jar");
        File external2 = new File(".m2/repository/com/fasterxml/classmate/1.0.0/classmate-1.0.0.jar");
        File external3 = new File(".m2/repository/commons-beanutils/commons-beanutils/1.8.3/commons-beanutils-1.8.3.jar");

        List<File> actual = Arrays.asList(external1, folder1, eric1, external2, folder2, eric2, external3);
        List<File> expected = Arrays.asList(eric1, eric2, folder1, folder2, external1, external2, external3);
        Collections.sort(actual, new ClassPathComparator());
        assertThat(actual, Matchers.contains(expected.toArray()));
    }

}