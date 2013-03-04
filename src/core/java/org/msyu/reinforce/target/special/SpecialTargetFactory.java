package org.msyu.reinforce.target.special;

import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetFactory;
import org.msyu.reinforce.TargetInvocation;

public class SpecialTargetFactory implements TargetFactory {

	public static final String NOP = "nop";

	static Target getTarget(String type, TargetInvocation invocation) {
		switch (type) {
			case NOP:
				return new NopTarget(invocation);
			default:
				return null;
		}
	}

	@Override
	public Target createTargetObject(String type, TargetInvocation invocation) {
		return getTarget(type, invocation);
	}

}
