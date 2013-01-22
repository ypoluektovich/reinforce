package org.msyu.reinforce.resources;

import org.msyu.reinforce.TargetInitializationException;

public class ResourceConstructionException extends TargetInitializationException {

	public ResourceConstructionException(String message) {
		super(message);
	}

	public ResourceConstructionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceConstructionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
