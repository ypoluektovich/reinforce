package org.msyu.reinforce;

public interface Reinterpretable {

	Object reinterpret(String interpretationSpec) throws ReinterpretationException;

}
