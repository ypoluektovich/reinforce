package org.msyu.reinforce.interpretation;

import org.msyu.reinforce.TargetInitializationException;

public abstract class ReinterpretationException extends TargetInitializationException {

	protected ReinterpretationException(String message) {
		super(message);
	}

	protected ReinterpretationException(String message, Throwable cause) {
		super(message, cause);
	}

	protected ReinterpretationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
