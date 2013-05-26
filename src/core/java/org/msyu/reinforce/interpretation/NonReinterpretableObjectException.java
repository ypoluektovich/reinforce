package org.msyu.reinforce.interpretation;

public class NonReinterpretableObjectException extends ReinterpretationException {

	private final Object myObject;

	public NonReinterpretableObjectException(Object object) {
		super("can't reinterpret non-Reinterpretable object: " + object);
		myObject = object;
	}

	public Object getObject() {
		return myObject;
	}

}
