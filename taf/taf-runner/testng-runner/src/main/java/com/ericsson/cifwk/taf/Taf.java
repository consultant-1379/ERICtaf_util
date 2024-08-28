package com.ericsson.cifwk.taf;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.management.TafBootstrap;
import com.ericsson.cifwk.taf.testng.TafTestRunnerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestRunnerFactory;
import org.testng.TestNG;
import org.testng.internal.Utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>Goal of this class is to wrap execution of test cases, so Taf runner will be
 * used.</p>
 *
 * <p>Usage is deprecated, please use
 * <a href="http://confluence-oss.lmera.ericsson.se/display/TAF/TAF+Surefire+provider">TAF provider</a>
 * for Maven Surefire plugin instead.</p>
 *
 * @deprecated
 */
@API(API.Quality.Deprecated)
@API.Since(2.15)
@Deprecated
public class Taf {

	public static final String FAILED_TO_INSTANTIATE_LISTENER = "Failed to instantiate Listener : ";
	private static final Logger LOGGER = LoggerFactory.getLogger(Taf.class);

	static final String SUITES = "suites";
	static final String DIR = "dir";
	static final String GROUPS = "groups";
	static final String LISTENER = "tafListeners";
	static final String SUITE_POOL_SIZE = "suitethreadpoolsize";

	static final Integer DEFAULT_SUITE_POOL_SIZE = 250;

	/**
	 * Command line entry point for TAF
	 * <p/>
	 * <code>-dir {path}</code> - directory where to look for xml suite
	 * definitions
	 * <p/>
	 * In addition the following System arguments are supported:
	 * <p/>
	 * <code>-suites {suite1, suite2}</code> - comma separated list of suite
	 * names <br/>
	 * <code>-groups {group1, group2}</code> - specifies TestNG groups to run <br/>
	 * <code>-listener {class name}</code> - adds custom TestNG listeners
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) throws Exception {
		TafBootstrap bootstrap = new TafBootstrap();
		bootstrap.init();

		try {
			Taf taf = new Taf();
			taf.go(args);
		} finally {
			bootstrap.shutdown();
		}
	}

	void go(String... args) {

		RelaxedTestNG testNG = createTestNG();
		testNG.setTestRunnerFactory(new TafTestRunnerFactory());

		String groupsArgument = System.getProperty(GROUPS);
		if (groupsArgument != null) {
			testNG.setGroups(groupsArgument);
		}

		String suiteThreadPoolSize = System.getProperty(SUITE_POOL_SIZE);
		if (suiteThreadPoolSize != null) {
			testNG.setSuiteThreadPoolSize(Integer.parseInt(suiteThreadPoolSize));
		} else {
			testNG.setSuiteThreadPoolSize(DEFAULT_SUITE_POOL_SIZE);
		}

		String listenerArgument = System.getProperty(LISTENER);
		if (listenerArgument != null) {
			String[] classNames = Utils.split(listenerArgument, ",");
			for (String className : classNames) {
				Object listener;
				try {
					listener = Class.forName(className).newInstance();
					testNG.addListener(listener);
				} catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
					LOGGER.error(FAILED_TO_INSTANTIATE_LISTENER + className, e);
				}
            }
		}

		String[] suites = findSuitesToRun(args);
		if (suites.length == 0) {
			LOGGER.warn("No test suites to run. Exiting.");
			return;
		}

		testNG.setTestSuites(Arrays.asList(suites));
		testNG.run();
	}

	private String[] findSuitesToRun(String[] args) {
		String suitesArgument = System.getProperty(SUITES);
		String dirArgument = extractArgumentValue(args, "-" + DIR);

		if (dirArgument == null) {
			LOGGER.warn("Supply test suites directory with -dir argument");
			return new String[] {};
		}

		String[] suites;
		if (suitesArgument == null) {
			suites = locateSuitesInDirectory(dirArgument);
		} else {
			suites = Utils.split(suitesArgument, ",");
			for (int i = 0; i < suites.length; i++) {
				String suite = suites[i];
				suites[i] = dirArgument + "/" + suite;
			}
		}
		return suites;
	}

	private static String extractArgumentValue(final String[] args, final String key) {
		final List<String> arguments = Arrays.asList(args);
		final int position = arguments.indexOf(key);
		if (position >= 0 && position + 1 <= arguments.size()) {
			return arguments.get(position + 1);
		}
		return null;
	}

	private static String[] locateSuitesInDirectory(final String dir) {
		LOGGER.info("Looking for suite.xml in directory : " + dir);

		final List<String> suites = new ArrayList<>();
		try {
			Path startPath = Paths.get(dir);
			Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file,
						BasicFileAttributes attrs) {
					if (file.toString().endsWith(".xml")) {
						suites.add(file.toString());
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			LOGGER.error("Suite files discovery failed.", e);
		}

		Collections.sort(suites);
		return suites.toArray(new String[suites.size()]);
	}

	RelaxedTestNG createTestNG() {
		return new RelaxedTestNG();
	}

	static class RelaxedTestNG extends TestNG {
		@Override
		public void setTestRunnerFactory(ITestRunnerFactory factory) {
			super.setTestRunnerFactory(factory);
		}
	}

}
