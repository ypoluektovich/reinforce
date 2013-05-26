package org.msyu.reinforce.interpretation;

public class InvalidInterpretationSpecException extends ReinterpretationException {

	private final Object mySpec;

	public InvalidInterpretationSpecException(Object spec) {
		super("reinterpretation specification must be a string or a list of specifications," +
				" of which " + getSpecClassName(spec) + " is neither");
		mySpec = spec;
	}

	private static String getSpecClassName(Object spec) {
		return (spec == null) ? "null" : spec.getClass().getName();
	}

	public Object getSpec() {
		return mySpec;
	}

}
