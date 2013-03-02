package org.msyu.reinforce;

public class CyclicTargetDependencyException extends TargetLoadingException {

	private final TargetInvocation myTargetInvocation;

	public CyclicTargetDependencyException(TargetInvocation invocation) {
		this.myTargetInvocation = invocation;
	}

	@Override
	public String getMessage() {
		return String.format("Detected a dependency cycle starting with the target %s", myTargetInvocation);
	}

}
