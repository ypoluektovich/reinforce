package org.msyu.reinforce;

public interface TargetRepository {

	/**
	 * Get a {@code Target} object for the specified {@code TargetInvocation}.
	 *
	 * @param invocation the invocation that the resulting {@code Target} is to handle.
	 *
	 * @return a new {@code Target}.
	 *
	 * @throws TargetNotFoundException if this {@code TargetRepository} fails to find the
	 * definition of a target corresponding to the specified invocation.
	 * @throws TargetLoadingException  if the target definition is found,
	 * but there are problems while loading and processing it.
	 */
	Target getTarget(TargetInvocation invocation) throws TargetNotFoundException, TargetLoadingException;

}
