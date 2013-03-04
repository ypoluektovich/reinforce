package org.msyu.reinforce.target.special;

import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.TargetLoadingException;
import org.msyu.reinforce.TargetNotFoundException;
import org.msyu.reinforce.TargetRepository;

public final class SpecialTargetRepository implements TargetRepository {

	@Override
	public final Target getTarget(TargetInvocation invocation) throws TargetNotFoundException, TargetLoadingException {
		Target target = SpecialTargetFactory.getTarget(invocation.getTargetName(), invocation);
		if (target == null) {
			throw new TargetNotFoundException(invocation.getTargetName());
		}
		return target;
	}

}
