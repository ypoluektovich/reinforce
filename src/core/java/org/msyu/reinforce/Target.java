package org.msyu.reinforce;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class Target implements Reinterpretable {

	private final String name;

	private final Set<String> dependencyTargetNames = new LinkedHashSet<>();

	private Map myDefinitionDocument;

	public Target(String name) {
		this.name = name;
	}

	void setDependencyTargetNames(Set<String> names) {
		dependencyTargetNames.addAll(names);
	}

	void setDefinitionDocument(Map docMap) {
		myDefinitionDocument = docMap;
	}


	public final String getName() {
		return name;
	}

	public Set<String> getDependencyTargetNames() {
		return Collections.unmodifiableSet(dependencyTargetNames);
	}


	void init(Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		if (myDefinitionDocument == null) {
			return;
		}
		initTarget(myDefinitionDocument, dependencyTargetByName);
		myDefinitionDocument = null;
	}

	protected abstract void initTarget(Map docMap, Map<String, Target> dependencyTargetByName)
			throws TargetInitializationException;


	public abstract void run() throws ExecutionException;


	@Override
	public Object reinterpret(String interpretationSpec) throws ReinterpretationException {
		throw new UnknownInterpretationException(interpretationSpec);
	}

}
