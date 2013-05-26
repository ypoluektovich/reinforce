package org.msyu.reinforce.interpretation;

public interface Reinterpretable {

	String DEFAULT_INTERPRETATION_SPEC = "";

	Object reinterpret(String interpretationSpec) throws ReinterpretationException;

}
