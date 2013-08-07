package org.msyu.reinforce.target.testing;

import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetFactory;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.target.testing.junit.JUnitTarget;
import org.msyu.reinforce.target.testing.testng.TestNgTarget;

public class TestingTargetFactory implements TargetFactory {

	public static final String JUNIT = "junit";
	public static final String TESTNG = "testng";

	@Override
	public Target createTargetObject(String type, TargetInvocation invocation) {
		switch (type) {
			case JUNIT:
				return new JUnitTarget(invocation);
			case TESTNG:
				return new TestNgTarget(invocation);
			default:
				return null;
		}
	}

}
