package org.msyu.reinforce.util.variables;

import org.msyu.reinforce.TargetLoadingException;

public class VariableSubstitutionException extends TargetLoadingException {

	public VariableSubstitutionException(String message) {
		super(message);
	}

	public VariableSubstitutionException(String message, Throwable cause) {
		super(message, cause);
	}

}
