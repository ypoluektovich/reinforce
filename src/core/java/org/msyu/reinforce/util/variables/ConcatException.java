package org.msyu.reinforce.util.variables;

public class ConcatException extends VariableSubstitutionException {

	public ConcatException(boolean leftIsString, boolean rightIsString) {
		super("can't concatenate " +
				(leftIsString ? "" : "non-") + "character and " +
				(leftIsString ? "" : "non-") + "character content"
		);
	}

}
