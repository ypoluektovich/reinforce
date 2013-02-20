package org.msyu.reinforce;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Reinforce {

	private static final String[] GERMAN_NUMERALS = {
			"Zwei", "Drei", "Vier", "Funf", "Sechs", "Sieben", "Acht", "Neun", "Zehn"
	};

	private static final AtomicInteger ourIndexSource = new AtomicInteger();

	private final int myIndex;

	private final Path myTargetDefLocation;

	private final YamlTargetLoader myTargetLoader;

	public Reinforce(Path targetDefLocation) {
		myIndex = ourIndexSource.incrementAndGet();
		Log.info("==== %s reporting!", this.toString());

		myTargetDefLocation = targetDefLocation.normalize();
		myTargetLoader = new YamlTargetLoader(new FileSystemTargetDefinitionStreamSource(myTargetDefLocation));
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
			Map<String, String> variables,
			Iterable<String> targetNames
	) throws BuildException {
		Build build = new Build(this, basePath.toAbsolutePath().normalize(), sandboxPath);
		if (variables != null) {
			for (Map.Entry<String, String> variable : variables.entrySet()) {
				build.setVariable(variable.getKey(), variable.getValue());
			}
		}
		build.executeOnce(targetNames);
		return build;
	}

	Target getTarget(String name) throws TargetLoadingException {
		return myTargetLoader.getTarget(name);
	}

}
