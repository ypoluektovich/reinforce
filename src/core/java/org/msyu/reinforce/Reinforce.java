package org.msyu.reinforce;

import java.nio.file.Path;
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

	public Build executeNewBuild(Path basePath, Path sandboxPath, Iterable<String> targetNames) throws BuildException {
		Build build = new Build(this, basePath.toAbsolutePath().normalize(), sandboxPath);
		Log.info("Requested targets in order: %s", targetNames);
		build.executeOnce(targetNames);
		Log.info("==== %s is done!", this);
		return build;
	}

	Target getTarget(String name) throws TargetDefinitionLoadingException, TargetConstructionException {
		return myTargetLoader.getTarget(name);
	}

}
