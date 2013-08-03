package org.msyu.reinforce.target.testing.junit;

import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetFactory;
import org.msyu.reinforce.TargetInvocation;

public class JUnitTargetFactory implements TargetFactory {

	public static final String JUNIT = "junit";

	@Override
	public Target createTargetObject(String type, TargetInvocation invocation) {
		switch (type) {
			case JUNIT:
				return new JUnitTarget(invocation);
			default:
				return null;
		}
	}

}
