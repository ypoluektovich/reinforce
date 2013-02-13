package org.msyu.reinforce;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Build {

	private static final ThreadLocal<Build> ourContextBuild = new InheritableThreadLocal<>();

	public static Build getCurrent() {
		return ourContextBuild.get();
	}

	private final Reinforce myReinforce;

	private final Path myBasePath;

	private final Path mySandboxPath;

	private final Set<String> myRequestedTargets = new HashSet<>();

	private final Map<String, Target> myExecutedTargets = new HashMap<>();

	private Target myCurrentTarget;

	public Build(Reinforce reinforce, Path basePath, Path sandboxPath) throws BuildException {
		this.myReinforce = reinforce;

		if (!Files.isDirectory(basePath)) {
			throw new InvalidBuildBaseException("specified base path is not a directory: " + basePath);
		}
		Log.verbose("Build base path is %s", basePath);
		this.myBasePath = basePath;
		Log.verbose("Build sandbox path is %s", sandboxPath);
		this.mySandboxPath = basePath.resolve(sandboxPath);
	}

	public Reinforce getReinforce() {
		return myReinforce;
	}

	public Path getBasePath() {
		return myBasePath;
	}

	public Path getSandboxPath() {
		return mySandboxPath;
	}

	public String getCurrentTargetName() {
		return myCurrentTarget.getName();
	}

	public void executeOnce(Iterable<String> targetNames) throws BuildException {
		for (String targetName : targetNames) {
			runRecursively(targetName);
		}
	}

	public void executeOnce(String targetName) throws BuildException {
		runRecursively(targetName);
	}

	private Target runRecursively(String targetName) throws BuildException {
		if (myRequestedTargets.contains(targetName)) {
			throw new CyclicTargetDependencyException(targetName);
		}
		myRequestedTargets.add(targetName);

		if (isTargetExecuted(targetName)) {
			return myExecutedTargets.get(targetName);
		}

		Log.info("== Loading target: %s", targetName);
		Target target = myReinforce.getTarget(targetName);

		Log.info("== Processing dependencies of target: %s", targetName);
		Map<String, Target> dependencyTargetByName = new HashMap<>();
		for (String depName : target.getDependencyTargetNames()) {
			Target depTarget = runRecursively(depName);
			dependencyTargetByName.put(depName, depTarget);
		}

		Log.info("== Initializing target: %s", targetName);
		target.init(dependencyTargetByName);

		execute(target);
		myExecutedTargets.put(targetName, target);

		return target;
	}

	private void execute(Target target) throws BuildException {
		Build previousBuild = ourContextBuild.get();
		ourContextBuild.set(this);
		Target previousTarget = myCurrentTarget;
		myCurrentTarget = target;
		try {
			Log.info("== Executing target: %s", target.getName());
			target.run();
			myExecutedTargets.put(target.getName(), target);
			Log.info("== Executed target: %s", target.getName());
		} finally {
			myCurrentTarget = previousTarget;
			ourContextBuild.set(previousBuild);
		}
	}

	public boolean isTargetExecuted(String targetName) {
		return myExecutedTargets.containsKey(targetName);
	}

}
