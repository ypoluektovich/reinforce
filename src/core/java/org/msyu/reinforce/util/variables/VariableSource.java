package org.msyu.reinforce.util.variables;

public interface VariableSource {

	boolean isDefined(String variableName);

	Object getValueOf(String variableName) throws UndefinedVariableException;

}
