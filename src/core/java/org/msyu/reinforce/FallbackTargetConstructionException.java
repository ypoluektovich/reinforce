package org.msyu.reinforce;

import java.util.Objects;

public class FallbackTargetConstructionException extends TargetConstructionException {

	private final TargetInvocation myFallbackTarget;

	public FallbackTargetConstructionException(TargetInvocation fallbackTarget, Throwable cause) {
		super("fall back to target " + fallbackTarget, cause);
		myFallbackTarget = Objects.requireNonNull(fallbackTarget, "fallback target must not be null");
	}

	public TargetInvocation getFallbackTarget() {
		return myFallbackTarget;
	}

}
