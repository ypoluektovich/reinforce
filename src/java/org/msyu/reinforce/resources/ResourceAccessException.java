package org.msyu.reinforce.resources;

import org.msyu.reinforce.BuildException;

public class ResourceAccessException extends BuildException {

	public ResourceAccessException() {
	}

	public ResourceAccessException(String message) {
		super(message);
	}

	public ResourceAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceAccessException(Throwable cause) {
		super(cause);
	}

	public ResourceAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
