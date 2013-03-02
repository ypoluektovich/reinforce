package org.msyu.reinforce.util.variables;

public interface VariableSource {

	boolean isDefined(String variableName);

	String getValueOf(String variableName) throws UndefinedVariableException;

}
