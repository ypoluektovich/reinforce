package org.msyu.reinforce;

public class CyclicTargetDependencyException extends TargetLoadingException {

	private final String targetName;

	public CyclicTargetDependencyException(String targetName) {
		this.targetName = targetName;
	}

	@Override
	public String getMessage() {
		return String.format("Detected a dependency cycle starting with the target %s", targetName);
	}

}
