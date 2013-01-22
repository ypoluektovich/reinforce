package org.msyu.reinforce;

public class TargetDefinitionLoadingException extends TargetLoadingException {

	protected TargetDefinitionLoadingException() {
	}

	public TargetDefinitionLoadingException(String message) {
		super(message);
	}

	public TargetDefinitionLoadingException(String message, Throwable cause) {
		super(message, cause);
	}

	public TargetDefinitionLoadingException(Throwable cause) {
		super(cause);
	}

	public TargetDefinitionLoadingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
