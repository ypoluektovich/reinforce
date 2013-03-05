package org.msyu.reinforce.util.definition;

import org.msyu.reinforce.TargetInitializationException;

public class AmbiguousDefinitionException extends TargetInitializationException {

	public AmbiguousDefinitionException() {
		super("can't decide which parser to use");
	}

}
