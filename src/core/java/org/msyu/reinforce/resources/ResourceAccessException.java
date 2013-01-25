package org.msyu.reinforce.resources;

import java.io.IOException;

public class ResourceAccessException extends IOException {

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

}
