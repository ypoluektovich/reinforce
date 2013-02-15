package org.msyu.reinforce.util.variables;

public class UnclosedVariableException extends VariableSubstitutionException {

	protected UnclosedVariableException() {
		super("all opened variable braces must be closed");
	}

}
