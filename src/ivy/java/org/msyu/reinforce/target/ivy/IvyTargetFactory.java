package org.msyu.reinforce.target.ivy;

import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetFactory;
import org.msyu.reinforce.TargetInvocation;

public class IvyTargetFactory implements TargetFactory {

	public static final String IVY_RETRIEVE = "ivy-retrieve";

	@Override
	public Target createTargetObject(String type, TargetInvocation targetInvocation) {
		switch (type) {
			case IVY_RETRIEVE:
				return new IvyRetrieveTarget(targetInvocation);
			default:
				return null;
		}
	}

}
