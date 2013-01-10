package org.msyu.reinforce;

public class TargetDefinitionNotFoundException extends TargetDefinitionLoadingException {

	public TargetDefinitionNotFoundException(final String targetName) {
		super(targetName);
	}

	@Override
	public String getMessage() {
		return String.format("Couldn't find definition of target: %s", super.getMessage());
	}
}
