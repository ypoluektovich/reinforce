package org.msyu.reinforce.target.junit;

import org.hamcrest.Matcher;
import org.junit.runner.JUnitCore;
import org.msyu.reinforce.Build;
import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.resources.ResourceCollections;
import org.msyu.reinforce.util.FilesUtil;
import org.msyu.reinforce.util.definition.SettingParserUtil;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JUnitTarget extends Target {

	public static final String TESTS_KEY = "tests";

	public static final String TEST_CLASSPATH_KEY = "classpath";

	private String myJavaExecutable;

	private Set<String> myRunnerClasspath;

	private final List<TestDescription> myTestDescriptions = new ArrayList<>();


	public JUnitTarget(TargetInvocation invocation) {
		super(invocation);
	}


	@Override
	protected void initTarget(Map docMap) throws TargetInitializationException {
		initJavaExecutable();
		initJUnitRunnerLocation();
		initTestDescriptions(docMap);
	}

	private void initJavaExecutable() {
		myJavaExecutable = "java";
	}

	private void initJUnitRunnerLocation() throws TargetInitializationException {
		myRunnerClasspath = new LinkedHashSet<>();
		try {
			myRunnerClasspath.add(getClasspathEntryString(JUnitRunner.class));
			myRunnerClasspath.add(getClasspathEntryString(JUnitCore.class));
			myRunnerClasspath.add(getClasspathEntryString(JUnitCore.class));
			myRunnerClasspath.add(getClasspathEntryString(Matcher.class));
		} catch (Exception e) {
			throw new TargetInitializationException("failed to compose JUnitRunner classpath");
		}
		Log.debug("JUnitRunner classpath: %s", myRunnerClasspath);
	}

	private String getClasspathEntryString(Class<?> clazz) throws TargetInitializationException {
		URI location;
		try {
			location = clazz.getProtectionDomain().getCodeSource().getLocation().toURI();
		} catch (URISyntaxException e) {
			throw new TargetInitializationException("can't parse location of class " + clazz.getName() + " as URI", e);
		}
		if (!"file".equals(location.getScheme())) {
			throw new TargetInitializationException("can't launch external JVM with non-file classpath");
		}
		return location.getSchemeSpecificPart();
	}

	private void initTestDescriptions(Map docMap) throws TargetInitializationException {
		if (!docMap.containsKey(TESTS_KEY)) {
			throw new TargetInitializationException("no tests to run specified");
		}
		Object tests = docMap.get(TESTS_KEY);
		if (tests instanceof List) {
			for (Object test : (List) tests) {
				initTestDescription(test);
			}
		} else {
			initTestDescription(tests);
		}
	}

	private void initTestDescription(Object test) throws TargetInitializationException {
		if (!(test instanceof Map)) {
			throw new TargetInitializationException("tests to run must be specified as a mapping or a list of mappings");
		}
		Map testMap = (Map) test;
		TestDescription testDescription = new TestDescription();
		initTestClasses(testMap, testDescription);
		if (testMap.containsKey(TEST_CLASSPATH_KEY)) {
			testDescription.addAllToClasspath(ResourceCollections.interpret(testMap.get(TEST_CLASSPATH_KEY)));
		}
		myTestDescriptions.add(testDescription);
	}

	private void initTestClasses(Map testMap, TestDescription testDescription) throws TargetInitializationException {
		try {
			SettingParserUtil.parseSingle(
					testMap,
					Arrays.asList(
							new AllInCollectionTestParser(testDescription)
					)
			);
		} catch (TargetInitializationException e) {
			throw new TargetInitializationException("could not determine what tests to run", e);
		}
	}


	@Override
	public void run() throws ExecutionException {
		Path testDescriptionsDir = prepareDescriptionDir();
		for (int i = 0; i < myTestDescriptions.size(); i++) {
			TestDescription testDescription = myTestDescriptions.get(i);
			Path testDescFile = prepareDescriptionFile(testDescriptionsDir, i, testDescription);
			executeRunner(testDescFile);
		}
	}

	private Path prepareDescriptionDir() throws ExecutionException {
		Path testDescriptionsDir = Build.getCurrent().getSandboxPath().resolve(getInvocation().getTargetName());
		try {
			FilesUtil.deleteFileTree(testDescriptionsDir);
			Files.createDirectories(testDescriptionsDir);
		} catch (IOException e) {
			throw new ExecutionException("error while preparing directory for test descriptions", e);
		}
		return testDescriptionsDir;
	}

	private Path prepareDescriptionFile(Path testDescriptionsDir, int testIndex, TestDescription testDescription) throws ExecutionException {
		Path testDescriptionFile = testDescriptionsDir.resolve("tests." + testIndex + ".dat");
		try (ObjectOutput output = new ObjectOutputStream(Files.newOutputStream(testDescriptionFile))) {
			testDescription.serialize(output);
		} catch (IOException e) {
			throw new ExecutionException("error while writing test description file");
		}
		return testDescriptionFile;
	}

	private void executeRunner(Path testDescFile) throws ExecutionException {
		int exitCode;
		try {
			List<String> commandLine = getCommandLineArray(testDescFile);
			Log.debug("Command line arguments: %s", commandLine);
			Log.verbose("Starting external process...");
			Process process = new ProcessBuilder(commandLine)
					.directory(Build.getCurrent().getBasePath().toFile())
					.inheritIO()
					.start();
			while (true) {
				try {
					exitCode = process.waitFor();
					break;
				} catch (InterruptedException e) {
					Log.debug("The thread waiting for the JUnit runner got interrupted. Ignoring...");
				}
			}
		} catch (IOException e) {
			throw new ExecutionException("exception during JUnit invocation", e);
		}
		Log.verbose("Process exited with code %d", exitCode);
		if (exitCode != 0) {
			throw new ExecutionException("JUnit runner exited with status code: " + exitCode);
		}
	}

	private List<String> getCommandLineArray(Path testDescFile) {
		List<String> commandArgs = new ArrayList<>();

		commandArgs.add(myJavaExecutable);

		commandArgs.add("-cp");
		commandArgs.add(getRunnerClasspathString());

		commandArgs.add(JUnitRunner.class.getName());

		commandArgs.add(testDescFile.toString());

		return commandArgs;
	}

	private String getRunnerClasspathString() {
		StringBuilder sb = new StringBuilder();
		for (String entry : myRunnerClasspath) {
			if (sb.length() > 0) {
				sb.append(System.getProperty("path.separator"));
			}
			sb.append(entry);
		}
		return sb.toString();
	}

}
