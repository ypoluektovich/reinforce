package org.msyu.reinforce.target.ivy;

import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetFactory;

public class IvyTargetFactory implements TargetFactory {

	public static final String IVY_RETRIEVE = "ivy-retrieve";

	@Override
	public Target createTargetObject(String type, String name) {
		switch (type) {
			case IVY_RETRIEVE:
				return new IvyRetrieveTarget(name);
			default:
				return null;
		}
	}

}
