package org.msyu.reinforce.target.testing.junit;

import org.junit.runner.Result;

public class TestsFailedException extends JUnitRunnerException {

	private final Result myResult;

	TestsFailedException(Result result) {
		super("tests failed");
		myResult = result;
	}

	public final Result getResult() {
		return myResult;
	}

}
