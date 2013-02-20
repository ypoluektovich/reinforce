package org.msyu.reinforce.util.variables;

public class TrailingEscapeException extends VariableSubstitutionException {

	protected TrailingEscapeException() {
		super("a variable-expanded string must not end with the escape character");
	}

}
