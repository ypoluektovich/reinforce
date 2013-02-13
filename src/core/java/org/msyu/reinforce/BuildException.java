package org.msyu.reinforce;

public abstract class BuildException extends Exception {

	protected BuildException() {
	}

	public BuildException(String message) {
		super(message);
	}

	public BuildException(String message, Throwable cause) {
		super(message, cause);
	}

	public BuildException(Throwable cause) {
		super(cause);
	}

	public BuildException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
