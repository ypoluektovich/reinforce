package org.msyu.reinforce;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
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

	private final Map<String, String> myVariables = new HashMap<>();

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

	public Map<String, String> getVariables() {
		return Collections.unmodifiableMap(myVariables);
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

		Target target;
		Map<String, Target> dependencyTargetByName;
		try {
			if (isTargetExecuted(targetName)) {
				return myExecutedTargets.get(targetName);
			}

			Log.info("== Loading target: %s", targetName);
			target = myReinforce.getTarget(targetName);

			Log.info("== Processing dependencies of target: %s", targetName);
			dependencyTargetByName = new HashMap<>();
			for (String depName : target.getDependencyTargetNames()) {
				Target depTarget = runRecursively(depName);
				dependencyTargetByName.put(depName, depTarget);
			}
		} catch (FallbackTargetConstructionException e) {
			String fallbackTargetName = e.getFallbackTargetName();
			Log.info("== Could not construct target %s; falling back on %s", targetName, fallbackTargetName);
			return runRecursively(fallbackTargetName);
		} finally {
			myRequestedTargets.remove(targetName);
		}

		execute(target, dependencyTargetByName);
		myExecutedTargets.put(targetName, target);

		return target;
	}

	private void execute(Target target, Map<String, Target> dependencyTargetByName) throws BuildException {
		Build previousBuild = ourContextBuild.get();
		ourContextBuild.set(this);
		Target previousTarget = myCurrentTarget;
		myCurrentTarget = target;
		try {
			Log.info("== Initializing target: %s", target.getName());
			target.init(dependencyTargetByName);
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

	/**
	 * Get the object for a target with the specified name that was executed in this build.
	 *
	 * @param targetName    the name of the target.
	 * @return the Target object, or {@code null} if this target has not been executed in this build.
	 */
	public Target getExecutedTarget(String targetName) {
		return myExecutedTargets.get(targetName);
	}

}
