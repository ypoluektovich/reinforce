package org.msyu.reinforce;

import java.util.HashSet;
import java.util.Set;

public class Build {

	private static final ThreadLocal<Build> ourContextBuild = new InheritableThreadLocal<>();

	public static Build getCurrent() {
		return ourContextBuild.get();
	}

	private final TargetRepository myTargetRepository;

	private final Set<Target> executedTargets = new HashSet<>();

	public Build(TargetRepository targetRepository) {
		this.myTargetRepository = targetRepository;
	}

	public void executeOnce(Iterable<String> targetNames) throws NoSuchTargetException, BuildException {
		for (String targetName : targetNames) {
			runRecursively(targetName);
		}
	}

	private void runRecursively(String targetName) throws NoSuchTargetException, BuildException {
		Target target = myTargetRepository.getTarget(targetName);
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
		try {
			Log.info("== Executing target: %s", target.getName());
			target.run();
			setTargetExecuted(true, target);
			Log.info("== Executed target: %s", target.getName());
		} finally {
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
