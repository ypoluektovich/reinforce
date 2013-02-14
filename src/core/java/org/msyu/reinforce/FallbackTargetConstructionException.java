package org.msyu.reinforce;

public class FallbackTargetConstructionException extends TargetConstructionException {

	private final String myFallbackTargetName;

	public FallbackTargetConstructionException(String fallbackTargetName, Throwable cause) {
		super("fall back to target " + fallbackTargetName, cause);
		myFallbackTargetName = fallbackTargetName;
	}

	public String getFallbackTargetName() {
		return myFallbackTargetName;
	}

}
