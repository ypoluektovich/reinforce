package org.msyu.reinforce.util.variables;

public class NonStringVariableNameException extends VariableSubstitutionException {

	protected NonStringVariableNameException() {
		super("variable names must be strings");
	}
}
