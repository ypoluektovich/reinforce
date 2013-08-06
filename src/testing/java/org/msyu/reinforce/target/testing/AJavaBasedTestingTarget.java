package org.msyu.reinforce.target.testing;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Log;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.resources.ResourceCollections;
import org.msyu.reinforce.target.ActionOnEmptySource;
import org.msyu.reinforce.util.FilesUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AJavaBasedTestingTarget<D extends ATestDescription> extends Target {

	public static final String TESTS_KEY = "tests";

	public static final String TEST_CLASSPATH_KEY = "classpath";

	public static final String ON_MISSING_KEY = "on missing";


	protected final Set<String> myRunnerClasspath = new LinkedHashSet<>();

	private String myJavaExecutable;

	protected final List<D> myTestDescriptions = new ArrayList<>();

	public AJavaBasedTestingTarget(TargetInvocation invocation) {
		super(invocation);
	}


	@Override
	protected final void initTarget(Map docMap) throws TargetInitializationException {
		initJavaExecutable();
		initRunnerClasspath();
		initTestDescriptions(docMap);
	}

	protected final void initJavaExecutable() {
		myJavaExecutable = "java";
	}

	void initRunnerClasspath() throws TargetInitializationException {
		try {
			for (Class<?> clazz : getRequiredClassesForRunner()) {
				myRunnerClasspath.add(getClasspathEntryString(clazz));
			}
		} catch (Exception e) {
			throw new TargetInitializationException("failed to compose test runner classpath");
		}
		Log.debug("Test runner classpath: %s", myRunnerClasspath);
	}

	protected String getClasspathEntryString(Class<?> clazz) throws TargetInitializationException {
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

	protected abstract Set<Class<?>> getRequiredClassesForRunner();

	private void initTestDescriptions(Map docMap) throws TargetInitializationException {
		if (!docMap.containsKey(TESTS_KEY)) {
			throw new TargetInitializationException("no tests to run specified");
		}
		Object tests = docMap.get(TESTS_KEY);
		if (tests instanceof Collection) {
			for (Object test : (Collection) tests) {
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
		D testDescription = newTestDescription(ActionOnEmptySource.parse(testMap, ON_MISSING_KEY), testMap);
		if (testDescription == null) {
			return;
		}
		if (testMap.containsKey(TEST_CLASSPATH_KEY)) {
			testDescription.addAllToClasspath(ResourceCollections.interpret(testMap.get(TEST_CLASSPATH_KEY)));
		}
		myTestDescriptions.add(testDescription);
	}

	protected abstract D newTestDescription(ActionOnEmptySource onMissing, Map testMap) throws TargetInitializationException;


	public final String getJavaExecutable() {
		return myJavaExecutable;
	}


	@Override
	public final void run() throws ExecutionException {
		Path workingDir = prepareWorkingDir();
		for (int i = 0; i < myTestDescriptions.size(); i++) {
			run(myTestDescriptions.get(i), i, workingDir);
		}
	}

	private Path prepareWorkingDir() throws ExecutionException {
		Path workingDir = Build.getCurrent().getSandboxPath().resolve(getInvocation().getTargetName());
		try {
			FilesUtil.deleteFileTree(workingDir);
			Files.createDirectories(workingDir);
		} catch (IOException e) {
			throw new ExecutionException("error while preparing working directory for running test", e);
		}
		return workingDir;
	}

	protected abstract void run(D testDescription, int index, Path workingDir) throws ExecutionException;

}
