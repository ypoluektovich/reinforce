package org.msyu.reinforce.util.variables;

public class UndefinedVariableException extends VariableSubstitutionException {
	protected UndefinedVariableException(String variableName) {
		super("variable '" + variableName + "' is not defined");
	}
}
