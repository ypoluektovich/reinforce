package org.msyu.reinforce;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class Target implements Reinterpretable {

	private final TargetInvocation myInvocation;

	private Set<TargetInvocation> myDependencyTargets;

	private Map myDefinitionDocument;

	public Target(TargetInvocation invocation) {
		this.myInvocation = invocation;
	}

	void setDependencyTargets(Set<TargetInvocation> invocations) {
		myDependencyTargets = new LinkedHashSet<>(invocations);
	}

	void setDefinitionDocument(Map docMap) {
		myDefinitionDocument = docMap;
	}


	public final TargetInvocation getInvocation() {
		return myInvocation;
	}

	public Set<TargetInvocation> getDependencyTargets() {
		return Collections.unmodifiableSet(myDependencyTargets);
	}


	void init() throws TargetInitializationException {
		if (myDefinitionDocument == null) {
			return;
		}
		initTarget(myDefinitionDocument);
		myDefinitionDocument = null;
	}

	protected abstract void initTarget(Map docMap)
			throws TargetInitializationException;


	public abstract void run() throws ExecutionException;


	@Override
	public Object reinterpret(String interpretationSpec) throws ReinterpretationException {
		throw new UnknownInterpretationException(interpretationSpec);
	}

}
