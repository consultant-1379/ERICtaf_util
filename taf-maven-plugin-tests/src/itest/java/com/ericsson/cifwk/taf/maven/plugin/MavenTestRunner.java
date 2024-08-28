package com.ericsson.cifwk.taf.maven.plugin;

import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ekonsla on 27/01/2016.
 */
public class MavenTestRunner {

    static void buildTestware() throws Exception {
        File workingDirectory = new File(Resources.getResource("taf-maven-plugin-testware").toURI());
        File pomFile = new File(workingDirectory, "pom.xml");

        InvocationRequest request = new DefaultInvocationRequest();
        request.setGoals(goals("clean", "install"));
        request.setPomFile(pomFile);

        Invoker invoker = new DefaultInvoker();
        invoker.setWorkingDirectory(workingDirectory);

        try {
            invoker.execute(request);
        } catch (MavenInvocationException e) {
            throw Throwables.propagate(e);
        }
    }

    public static String copyPomToTemp(String pomName) throws URISyntaxException, IOException {
        File inTarget = pom(pomName);
        String randomFolder = RandomStringUtils.randomAlphanumeric(5);
        File toDir = new File(inTarget.getParent() + "/" + randomFolder);
        FileUtils.copyFileToDirectory(inTarget, toDir);
        return randomFolder;
    }

    public static InvocationResult runPom(String pomName, String... goals) throws Exception {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setGoals(goals(goals));
        request.setPomFile(pom(pomName));

        Invoker invoker = new DefaultInvoker();

        try {
            return invoker.execute(request);
        } catch (MavenInvocationException e) {
            throw Throwables.propagate(e);
        }
    }

    private static File pom(String pomName) throws URISyntaxException {
        ClassLoader classLoader = MavenTestRunner.class.getClassLoader();
        URL resource = classLoader.getResource(pomName);
        if (resource == null) {
            throw new RuntimeException(String.format("File '%s' not found", pomName));
        }
        return new File(resource.toURI());
    }

    private static List<String> goals(String ... goals){
        return Arrays.asList(goals);
    }

}
