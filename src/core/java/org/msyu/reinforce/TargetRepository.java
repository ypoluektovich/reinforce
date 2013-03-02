package org.msyu.reinforce;

public interface TargetRepository {

	Target getTarget(TargetInvocation invocation) throws TargetLoadingException;

}
