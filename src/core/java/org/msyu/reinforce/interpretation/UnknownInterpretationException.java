package org.msyu.reinforce.interpretation;

public class UnknownInterpretationException extends ReinterpretationException {

	public UnknownInterpretationException(String interpretationSpec) {
		super("unknown interpretation: " + interpretationSpec);
	}

}
