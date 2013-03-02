package org.msyu.reinforce;

public class InvalidTargetDependencyException extends TargetConstructionException {

	public InvalidTargetDependencyException(String message) {
		super(message);
	}

	public InvalidTargetDependencyException(String message, Throwable cause) {
		super(message, cause);
	}

}
