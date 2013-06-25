package org.msyu.reinforce.util.variables;

final class EmptyVariableSource implements VariableSource {

	static final EmptyVariableSource INSTANCE = new EmptyVariableSource();

	@Override
	public final boolean isDefined(String variableName) {
		return false;
	}

	@Override
	public final Object getValueOf(String variableName) throws UndefinedVariableException {
		throw new UndefinedVariableException(variableName);
	}

}
