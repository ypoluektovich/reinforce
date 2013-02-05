package org.msyu.reinforce;

public class UnknownInterpretationException extends ReinterpretationException {

	public UnknownInterpretationException(String interpretationSpec) {
		super("unknown interpretation: " + interpretationSpec);
	}

}
