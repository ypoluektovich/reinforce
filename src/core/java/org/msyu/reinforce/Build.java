package org.msyu.reinforce;

import org.msyu.reinforce.util.variables.VariableOverwriteException;
import org.msyu.reinforce.util.variables.VariableSource;
import org.msyu.reinforce.util.variables.Variables;

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

	private final Map<String, Object> myVariables = new HashMap<>();

	private final Set<TargetInvocation> myRequestedTargets = new HashSet<>();

	private final Map<TargetInvocation, Target> myExecutedTargets = new HashMap<>();

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

	public VariableSource getBuildVariables() {
		return Variables.sourceFromMap(myVariables);
	}

	public VariableSource getContextVariables() {
		return (myCurrentTarget == null) ?
				getBuildVariables() :
				Variables.sourceFromChain(
						Variables.sourceFromMap(myCurrentTarget.getInvocation().getParameters()),
						getBuildVariables()
				);
	}

	public void setVariable(String name, Object value) throws VariableOverwriteException {
		if (myVariables.containsKey(name)) {
			throw new VariableOverwriteException(name);
		}
		myVariables.put(name, value);
	}

	public String getCurrentTargetName() {
		return myCurrentTarget.getInvocation().getTargetName();
	}

	public void executeOnce(TargetInvocation targetInvocation) throws BuildException {
		executeOnce(Collections.singletonList(targetInvocation));
	}

	public void executeOnce(Iterable<TargetInvocation> targetInvocations) throws BuildException {
		Log.info("==== Requested targets in order: %s", targetInvocations);
		for (TargetInvocation targetInvocation : targetInvocations) {
			runRecursively(targetInvocation);
		}
		Log.info("==== %s is done!", getReinforce());
	}

	private Target runRecursively(TargetInvocation targetInvocation) throws BuildException {
		if (myRequestedTargets.contains(targetInvocation)) {
			throw new CyclicTargetDependencyException(targetInvocation);
		}
		myRequestedTargets.add(targetInvocation);

		Target target;
		try {
			if (isTargetExecuted(targetInvocation)) {
				return myExecutedTargets.get(targetInvocation);
			}

			Log.info("== Loading target: %s", targetInvocation);
			target = myReinforce.getTarget(targetInvocation);

			Log.info("== Processing dependencies of target: %s", targetInvocation);
			for (TargetInvocation depInvocation : target.getDependencyTargets()) {
				runRecursively(depInvocation);
			}
		} catch (FallbackTargetConstructionException e) {
			TargetInvocation fallbackTargetInvocation = e.getFallbackTarget();
			Log.info("== Could not construct target %s; falling back on %s", targetInvocation, fallbackTargetInvocation);
			Target fallbackTarget = runRecursively(fallbackTargetInvocation);
			myExecutedTargets.put(targetInvocation, fallbackTarget);
			return fallbackTarget;
		} finally {
			myRequestedTargets.remove(targetInvocation);
		}

		execute(target);
		myExecutedTargets.put(targetInvocation, target);

		return target;
	}

	private void execute(Target target) throws BuildException {
		Build previousBuild = ourContextBuild.get();
		ourContextBuild.set(this);
		Target previousTarget = myCurrentTarget;
		myCurrentTarget = target;
		try {
			Log.info("== Initializing target: %s", target.getInvocation());
			target.init();
			Log.info("== Executing target: %s", target.getInvocation());
			target.run();
			Log.info("== Executed target: %s", target.getInvocation());
		} finally {
			myCurrentTarget = previousTarget;
			ourContextBuild.set(previousBuild);
		}
	}

	public boolean isTargetExecuted(TargetInvocation invocation) {
		return myExecutedTargets.containsKey(invocation);
	}

	/**
	 * Get the object for a target with the specified invocation that was executed in this build.
	 *
	 * @param invocation    the invocation of the target.
	 * @return the Target object, or {@code null} if this target has not been executed in this build.
	 */
	public Target getExecutedTarget(TargetInvocation invocation) {
		return myExecutedTargets.get(invocation);
	}

	/**
	 * Get the names of all targets executed in this {@code Build}.
	 *
	 * The collection returned by this method is a defensive copy.
	 *
	 * @return the set of executed target names.
	 */
	public Set<TargetInvocation> getExecutedTargets() {
		return new HashSet<>(myExecutedTargets.keySet());
	}

	public void setExecutedTarget(TargetInvocation invocation, Target target) throws ExecutionException {
		if (invocation == null) {
			throw new ExecutionException("target name must be non-null");
		}
		if (target == null) {
			throw new ExecutionException("target object must be non-null");
		}
		Log.debug("Set executed target: %s -> %s", invocation, target);
		myExecutedTargets.put(invocation, target);
	}

}
