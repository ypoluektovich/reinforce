package org.msyu.reinforce.target.testing.junit;

import org.hamcrest.Matcher;
import org.junit.runner.JUnitCore;
import org.msyu.reinforce.Build;
import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.target.ActionOnEmptySource;
import org.msyu.reinforce.target.testing.AJavaBasedTestingTarget;
import org.msyu.reinforce.util.definition.SettingParserUtil;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JUnitTarget extends AJavaBasedTestingTarget<TestDescription> {



	public JUnitTarget(TargetInvocation invocation) {
		super(invocation);
	}


	@Override
	protected final Set<Class<?>> getRequiredClassesForRunner() {
		return new HashSet<>(Arrays.asList(JUnitRunner.class, JUnitCore.class, Matcher.class));
	}


	@Override
	protected final TestDescription newTestDescription(ActionOnEmptySource onMissing, Map testMap) throws TargetInitializationException {
		TestDescription testDescription = new TestDescription(onMissing);
		try {
			SettingParserUtil.parseSingle(
					testMap,
					Arrays.asList(new AllInCollectionTestParser(testDescription))
			);
		} catch (TargetInitializationException e) {
			throw new TargetInitializationException("could not determine what tests to run", e);
		}
		return testDescription;
	}


	@Override
	protected void run(TestDescription testDescription, int index, Path workingDir) throws ExecutionException {
		Path testDescFile = prepareDescriptionFile(workingDir, index, testDescription);
		executeRunner(testDescFile);
	}

	private Path prepareDescriptionFile(Path workingDir, int testIndex, TestDescription testDescription) throws ExecutionException {
		Path testDescriptionFile = workingDir.resolve("tests." + testIndex + ".dat");
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

		commandArgs.add(getJavaExecutable());

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
