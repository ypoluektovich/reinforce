package org.msyu.reinforce;

public abstract class TargetLoadingException extends BuildException {

	protected TargetLoadingException() {
	}

	public TargetLoadingException(String message) {
		super(message);
	}

	public TargetLoadingException(String message, Throwable cause) {
		super(message, cause);
	}

	public TargetLoadingException(Throwable cause) {
		super(cause);
	}

	public TargetLoadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
