package org.msyu.reinforce.util.variables;

public final class ImpossibleIOException extends RuntimeException {

	ImpossibleIOException(Throwable cause) {
		super("impossible IO error while expanding variables", cause);
	}

}
