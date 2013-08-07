package org.msyu.reinforce.target.testing.testng;

import com.beust.jcommander.JCommander;
import org.junit.runner.JUnitCore;
import org.msyu.reinforce.Build;
import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.resources.FileSystemResource;
import org.msyu.reinforce.target.ActionOnEmptySource;
import org.msyu.reinforce.target.testing.AJavaBasedTestingTarget;
import org.msyu.reinforce.util.ExecUtil;
import org.msyu.reinforce.util.FilesUtil;
import org.msyu.reinforce.util.variables.VariableSubstitutionException;
import org.msyu.reinforce.util.variables.Variables;
import org.testng.TestNG;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestNgTarget extends AJavaBasedTestingTarget<TestDescription> {

	private static final String SUITE_KEY = "suite";

	private static final String MISSING_SUITE_MESSAGE = "Test suite not found where expected; skipping";


	public TestNgTarget(TargetInvocation invocation) {
		super(invocation);
	}

	@Override
	protected final Set<Class<?>> getRequiredClassesForRunner() {
		return new HashSet<>(Arrays.asList(TestNG.class, JCommander.class, JUnitCore.class));
	}


	@Override
	protected TestDescription newTestDescription(ActionOnEmptySource onMissing, Map testMap) throws TargetInitializationException {
		if (!testMap.containsKey(SUITE_KEY)) {
			throw new TargetInitializationException("missing test suite location");
		}
		Object suiteSetting = testMap.get(SUITE_KEY);
		if (!(suiteSetting instanceof String)) {
			throw new TargetInitializationException("test suite location must be specified as a string");
		}
		Object expandedSuiteSetting;
		try {
			expandedSuiteSetting = Variables.expand((String) suiteSetting);
		} catch (VariableSubstitutionException e) {
			throw new TargetInitializationException("error while expanding variables in test suite location", e);
		}
		if (!(expandedSuiteSetting instanceof String)) {
			throw new TargetInitializationException("test suite location must expand to a string");
		}
		Path suitePath;
		try {
			suitePath = Paths.get((String) expandedSuiteSetting);
		} catch (InvalidPathException e) {
			throw new TargetInitializationException("test suite location was not a valid file system path", e);
		}
		if (checkEmptySource(suitePath, onMissing)) {
			return null;
		}
		return new TestDescription(new FileSystemResource(suitePath, null, suitePath.getFileName()), onMissing);
	}

	private boolean checkEmptySource(Path testSuiteFile, ActionOnEmptySource actionOnMissingTests) throws TargetInitializationException {
		Log.debug("Checking that test suite is present...");
		if (Files.isRegularFile(testSuiteFile)) {
			return false;
		}
		if (actionOnMissingTests == ActionOnEmptySource.DIE) {
			throw new TargetInitializationException("test suite is missing or is not a file");
		}
		if (actionOnMissingTests == ActionOnEmptySource.WARN) {
			Log.warn(MISSING_SUITE_MESSAGE);
		} else {
			Log.info(MISSING_SUITE_MESSAGE);
		}
		return true;
	}


	@Override
	protected void run(TestDescription testDescription, int index, Path workingDir) throws ExecutionException {
		int exitCode = ExecUtil.execute(
				getCommandLineArray(
						testDescription.getClasspathEntries(),
						Build.getCurrent().getBasePath().resolve(testDescription.getSuite().getPath())
				),
				workingDir
		);
		if (exitCode != 0) {
			throw new ExecutionException("TestNG exited with non-zero code: " + exitCode);
		}
	}

	private List<String> getCommandLineArray(Set<URL> classpathEntries, Path testSuiteFile) {
		List<String> commandArgs = new ArrayList<>();

		commandArgs.add("java");

		commandArgs.add("-cp");
		commandArgs.add(getClasspathString(classpathEntries));

		commandArgs.add(TestNG.class.getName());

		commandArgs.add(testSuiteFile.toString());

		return commandArgs;
	}

	private String getClasspathString(Set<URL> classpathEntries) {
		StringBuilder sb = new StringBuilder();

		for (String entry : myRunnerClasspath) {
			if (sb.length() > 0) {
				sb.append(FilesUtil.PATH_SEPARATOR);
			}
			sb.append(entry);
		}

		for (URL classpathEntry : classpathEntries) {
			if (sb.length() > 0) {
				sb.append(FilesUtil.PATH_SEPARATOR);
			}
			sb.append(classpathEntry.toString().substring(classpathEntry.getProtocol().length() + 1));
		}

		return sb.toString();
	}

}
