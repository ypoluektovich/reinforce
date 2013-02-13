package org.msyu.reinforce;

public class TargetInitializationException extends TargetLoadingException {

	public TargetInitializationException(String message) {
		super(message);
	}

	public TargetInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public TargetInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
