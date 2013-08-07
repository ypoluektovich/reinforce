package org.msyu.reinforce.target.testing.testng;

import org.msyu.reinforce.resources.Resource;
import org.msyu.reinforce.target.ActionOnEmptySource;
import org.msyu.reinforce.target.testing.ATestDescription;

public class TestDescription extends ATestDescription {

	private final Resource mySuite;

	public TestDescription(Resource suite, ActionOnEmptySource actionOnMissingTests) {
		super(actionOnMissingTests);
		mySuite = suite;
	}

	public Resource getSuite() {
		return mySuite;
	}

}
