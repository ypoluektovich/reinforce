package org.msyu.reinforce.util.variables;

import org.msyu.reinforce.TargetLoadingException;

public class VariableSubstitutionException extends TargetLoadingException {

	protected VariableSubstitutionException(String message) {
		super(message);
	}

	protected VariableSubstitutionException(String message, Throwable cause) {
		super(message, cause);
	}

}
