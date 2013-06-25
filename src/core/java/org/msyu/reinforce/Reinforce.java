package org.msyu.reinforce;

import org.msyu.reinforce.target.special.SpecialTargetRepository;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Reinforce {

	private static final String[] GERMAN_NUMERALS = {
			"Zwei", "Drei", "Vier", "Funf", "Sechs", "Sieben", "Acht", "Neun", "Zehn"
	};

	private static final AtomicInteger ourIndexSource = new AtomicInteger();

	private final int myIndex;

	private final Path myTargetDefLocation;

	private final TargetRepository myTargetRepository;

	public Reinforce(Path targetDefLocation) {
		myIndex = ourIndexSource.incrementAndGet();
		Log.info("==== %s reporting!", this.toString());

		myTargetDefLocation = targetDefLocation.normalize();
		myTargetRepository = new ChainTargetRepository(Arrays.asList(
				new SpecialTargetRepository(),
				new YamlTargetLoader(new FileSystemTargetDefinitionStreamSource(myTargetDefLocation))
		));
		Log.verbose("Loading targets from %s", myTargetDefLocation);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Reinforce");
		if (myIndex >= 2) {
			sb.append(" ");
			sb.append((myIndex <= 10) ? GERMAN_NUMERALS[myIndex - 2] : String.valueOf(myIndex));
		}
		return sb.toString();
	}

	public Path getTargetDefLocation() {
		return myTargetDefLocation;
	}

	public Build executeNewBuild(
			Path basePath,
			Path sandboxPath,
			Map<String, Object> variables,
			Map<TargetInvocation, Target> inheritedTargets,
			Iterable<TargetInvocation> targetInvocations
	) throws BuildException {
		Build build = new Build(this, basePath.toAbsolutePath().normalize(), sandboxPath);
		if (variables != null) {
			for (Map.Entry<String, Object> variable : variables.entrySet()) {
				build.setVariable(variable.getKey(), variable.getValue());
			}
		}
		if (inheritedTargets != null) {
			for (Map.Entry<TargetInvocation, Target> inheritedTarget : inheritedTargets.entrySet()) {
				build.setExecutedTarget(inheritedTarget.getKey(), inheritedTarget.getValue());
			}
		}
		build.executeOnce(targetInvocations);
		return build;
	}

	Target getTarget(TargetInvocation invocation) throws TargetNotFoundException, TargetLoadingException {
		return myTargetRepository.getTarget(invocation);
	}

}
