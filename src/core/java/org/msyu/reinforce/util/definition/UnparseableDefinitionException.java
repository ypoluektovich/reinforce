package org.msyu.reinforce.util.definition;

import org.msyu.reinforce.TargetInitializationException;

public class UnparseableDefinitionException extends TargetInitializationException {

	public UnparseableDefinitionException() {
		super("none of the supplied parsers can handle the definition");
	}

}
