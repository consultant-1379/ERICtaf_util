package com.ericsson.cifwk.taf.testng;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This class updates the list of tests to be run based on the command line
 * parameter groups
 *
 * @author ethomev
 */
public class GroupsListener implements IMethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(GroupsListener.class);

    /**
     * Check each test methods Test Annotation for the groups parameter and see
     * does it match the groups parameter passed on the command line. If a
     * groups parameter is passed on the command line, all tests which don't
     * have this group are not added to the list of test methods to run.
     */
    @Override
    public List<IMethodInstance> intercept(final List<IMethodInstance> methods, final ITestContext context) {

        final List<IMethodInstance> methodsList = new ArrayList<>(methods);
        final String cmdLineGroups = getGroups();
        final List<String> suiteGroups = context.getCurrentXmlTest().getIncludedGroups();
        LOG.debug("Command line groups is {}. Suite groups is {}", cmdLineGroups, suiteGroups);
        if(!Strings.isNullOrEmpty(cmdLineGroups)){
            final List<String> commandLineGroups = Arrays.asList(StringUtils.split(cmdLineGroups, ","));
            return getMethodsToExecute(methodsList, commandLineGroups);
        } else if (!suiteGroups.isEmpty()){
            return getMethodsToExecute(methodsList, suiteGroups);
        } else {
            return methods;
        }
    }

    @VisibleForTesting
    String getGroups() {
        return System.getProperty("groups");
    }

    @VisibleForTesting
    protected List<IMethodInstance> getMethodsToExecute(final List<IMethodInstance> methods, final List<String> commandGroups) {

        final Iterator<IMethodInstance> iterator = methods.iterator();
        while (iterator.hasNext()) {
            final IMethodInstance method = iterator.next();
            final Method testMethod = method.getMethod().getConstructorOrMethod().getMethod();
            final List<String> testGroups = Arrays.asList(testMethod.getAnnotation(Test.class).groups());

            if (!isContainedInGroups(commandGroups, testGroups)) {
                iterator.remove();
            }
        }
        return methods;
    }

    @VisibleForTesting
    protected boolean isContainedInGroups(final List<String> commandGroups, final List<String> testGroups) {
        for (final String command : commandGroups) {
            if (testGroups.containsAll(splitAndTrim(command, " AND "))) {
                return true;
            }
        }
        return false;
    }

    private List<String> splitAndTrim(final String command, final String delimter) {
        final List<String> splitCommand = Arrays.asList(command.split(delimter));
        final List<String> splitCommandTrimmed = new ArrayList<>();

        for (final String group : splitCommand) {
            splitCommandTrimmed.add(group.trim());
        }

        return splitCommandTrimmed;

    }
}
