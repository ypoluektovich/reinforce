package org.msyu.reinforce.resources;

import org.msyu.reinforce.BuildException;

public class ResourceEnumerationException extends BuildException {

	public ResourceEnumerationException() {
	}

	public ResourceEnumerationException(String message) {
		super(message);
	}

	public ResourceEnumerationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceEnumerationException(Throwable cause) {
		super(cause);
	}

	public ResourceEnumerationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
