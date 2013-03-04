package org.msyu.reinforce;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ChainTargetRepository implements TargetRepository {

	private final List<TargetRepository> myChainedRepositories;

	public ChainTargetRepository(Collection<TargetRepository> chainedRepositories) {
		Objects.requireNonNull(chainedRepositories, "must specify target repository chain");
		if (chainedRepositories.isEmpty()) {
			throw new IllegalArgumentException("target repository chain must not be empty");
		}
		if (chainedRepositories.contains(null)) {
			throw new IllegalArgumentException("target repository chain must not contain nulls");
		}
		myChainedRepositories = new ArrayList<>(chainedRepositories);
	}

	@Override
	public Target getTarget(TargetInvocation invocation) throws TargetLoadingException, TargetNotFoundException {
		TargetNotFoundException notFoundException = null;
		for (TargetRepository repository : myChainedRepositories) {
			try {
				return repository.getTarget(invocation);
			} catch (TargetNotFoundException e) {
				if (notFoundException == null) {
					notFoundException = new TargetNotFoundException(invocation.getTargetName());
				}
				notFoundException.addSuppressed(e);
			}
		}
		// repository chain is not empty,
		// so if we're here, then at least one exception has been suppressed
		// (and the wrapper exception created)
		throw notFoundException;
	}

}
