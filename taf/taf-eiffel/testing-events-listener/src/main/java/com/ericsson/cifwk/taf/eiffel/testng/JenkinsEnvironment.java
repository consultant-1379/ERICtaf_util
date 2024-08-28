package com.ericsson.cifwk.taf.eiffel.testng;

/**
 * Retrieves Jenkins environment variables according to specification.
 * <p/>
 * https://wiki.jenkins-ci.org/display/JENKINS/Building+a+software+project#Buildingasoftwareproject-below
 */
public final class JenkinsEnvironment {

    // The current build number, such as "153"
    public static final String BUILD_NUMBER = "BUILD_NUMBER";
    // The current build id, such as "2005-08-22_23-59-59" (YYYY-MM-DD_hh-mm-ss)
    public static final String BUILD_ID = "BUILD_ID";
    // The URL where the results of this build can be found (e.g. http://buildserver/jenkins/job/MyJobName/666/)
    public static final String BUILD_URL = "BUILD_URL";
    // The name of the node the current build is running on. Equals 'master' for master node.
    public static final String NODE_NAME = "NODE_NAME";
    // Name of the project of this build. This is the name you gave your job when you first set it up.
    // It's the third column of the Jenkins Dashboard main page.
    public static final String JOB_NAME = "JOB_NAME";
    // Set to the URL of the Jenkins master that's running the build. This value is used by Jenkins CLI for example
    public static final String JENKINS_URL = "JENKINS_URL";
    // The unique number that identifies the current executor (among executors of the same machine)
    // that's carrying out this build. This is the number you see in the "build executor status",
    // except that the number starts from 0, not 1.
    public static final String EXECUTOR_NUMBER = "EXECUTOR_NUMBER";

    private JenkinsEnvironment() {
    }

    public static String getStringVariable(String key) {
        return System.getProperty(key);
    }

    public static Integer getIntegerVariable(String key) {
        return Integer.valueOf(System.getProperty(key, "0"));
    }

}
