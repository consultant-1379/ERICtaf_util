package com.ericsson.cifwk.taf.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ekonsla on 15/12/2015.
 */

@Mojo(name = "clean",
defaultPhase = LifecyclePhase.CLEAN,
requiresProject = true,
threadSafe = true)
public class TafCleanMojo extends AbstractMojo {

    @Component
    protected MavenProject project;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<String> clean = Arrays.asList(
                project.getBasedir() + "/test-output",
                project.getBasedir() + "/tmp"
        );

        for (String it : clean){
            if (new File(it).exists()) {
                getLog().debug("Deleting " + it);
                try {
                    FileUtils.forceDelete(it);
                } catch (IOException e) {
                    getLog().info("folder " + it + " doesn't exist");
                }
            }
        }
    }
}
