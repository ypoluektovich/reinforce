package org.msyu.reinforce;

public class ReinterpretationException extends TargetConstructionException {

	public ReinterpretationException(String message) {
		super(message);
	}

	public ReinterpretationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReinterpretationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
