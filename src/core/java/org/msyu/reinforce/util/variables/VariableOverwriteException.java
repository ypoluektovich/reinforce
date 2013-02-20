package org.msyu.reinforce.util.variables;

import org.msyu.reinforce.ExecutionException;

/**
 * @author Yanus Poluektovich (ypoluektovich@gmail.com)
 */
public class VariableOverwriteException extends ExecutionException {

	public VariableOverwriteException(String name) {
		super("cannot overwrite variable: " + name);
	}

}
