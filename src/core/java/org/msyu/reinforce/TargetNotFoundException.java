package org.msyu.reinforce;

public class TargetNotFoundException extends BuildException {

	public TargetNotFoundException(String targetName) {
		super(targetName);
	}

	@Override
	public String getMessage() {
		return String.format("Couldn't find definition of target: %s", super.getMessage());
	}

}
