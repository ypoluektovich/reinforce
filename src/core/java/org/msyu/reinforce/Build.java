package org.msyu.reinforce;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Build {

	private static final ThreadLocal<Build> ourContextBuild = new InheritableThreadLocal<>();

	public static Build getCurrent() {
		return ourContextBuild.get();
	}

	private final Reinforce myReinforce;

	private final Path myBasePath;

	private final Path mySandboxPath;

	private final Set<Target> executedTargets = new HashSet<>();

	private Target myCurrentTarget;

	public Build(Reinforce reinforce, Path basePath, Path sandboxPath) throws BuildException {
		this.myReinforce = reinforce;

		if (!Files.isDirectory(basePath)) {
			throw new BuildException("Specified base path is not a directory: " + basePath);
		}
		this.myBasePath = basePath;
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

	private void runRecursively(String targetName) throws BuildException {
		Target target = myReinforce.getTarget(targetName);
		if (isTargetExecuted(target)) {
			return;
		}
		for (String depName : target.getDependencyTargetNames()) {
			runRecursively(depName);
		}
		executeNow(target);
	}

	public void executeNow(Target target) throws BuildException {
		Build previousBuild = ourContextBuild.get();
		ourContextBuild.set(this);
		Target previousTarget = myCurrentTarget;
		myCurrentTarget = target;
		try {
			Log.info("== Executing target: %s", target.getName());
			target.run();
			setTargetExecuted(true, target);
			Log.info("== Executed target: %s", target.getName());
		} finally {
			myCurrentTarget = previousTarget;
			ourContextBuild.set(previousBuild);
		}
	}

	public boolean isTargetExecuted(Target target) {
		return executedTargets.contains(target);
	}

	public void setTargetExecuted(boolean executed, Target target) {
		if (executed) {
			executedTargets.add(target);
		} else {
			executedTargets.remove(target);
		}
	}

}
