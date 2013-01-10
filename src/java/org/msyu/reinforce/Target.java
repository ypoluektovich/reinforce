package org.msyu.reinforce;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class Target {

	private final String name;

	private final Set<String> dependencyTargetNames = new LinkedHashSet<>();

	public Target(String name) {
		this.name = name;
	}

	void setDependencyTargetNames(Set<String> names) {
		dependencyTargetNames.addAll(names);
	}


	public final String getName() {
		return name;
	}

	public Set<String> getDependencyTargetNames() {
		return Collections.unmodifiableSet(dependencyTargetNames);
	}

	protected abstract void initTarget(Map docMap, Map<String, Target> dependencyTargetByName)
			throws TargetInitializationException;

	public abstract void run() throws BuildException;
}
