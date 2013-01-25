package org.msyu.reinforce;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public class Reinforce implements TargetRepository {

	private static final String[] GERMAN_NUMERALS = {
			"Zwei", "Drei", "Vier", "Funf", "Sechs", "Sieben", "Acht", "Neun", "Zehn"
	};

	private static final AtomicInteger ourIndexSource = new AtomicInteger();

	private final int myIndex;

	public Reinforce() {
		myIndex = ourIndexSource.incrementAndGet();
		Log.info("==== %s reporting!", this.toString());
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

	private Path myTargetDefLocation;

	private YamlTargetLoader myTargetLoader;

	public void setTargetDefLocation(Path targetDefLocation) {
		myTargetDefLocation = targetDefLocation.normalize();
		myTargetLoader = new YamlTargetLoader(new FileSystemTargetDefinitionStreamSource(myTargetDefLocation));
		Log.verbose("Loading targets from %s", myTargetDefLocation);
	}

	public Path getTargetDefLocation() {
		return myTargetDefLocation;
	}

	public void executeNewBuild(Path basePath, Path sandboxPath, Iterable<String> targetNames)
			throws InvalidTargetNameException, TargetLoadingException, BuildException
	{
		basePath = basePath.toAbsolutePath().normalize();
		Log.verbose("Build base path is %s", basePath);
		sandboxPath = basePath.resolve(sandboxPath);
		Log.verbose("Build sandbox path is %s", sandboxPath);
		Log.verbose("Requested targets in order: %s", targetNames);

		Log.info("==== %s: loading targets", this);
		myTargetLoader.load(targetNames);

		Log.info("==== %s: executing targets", this);
		new Build(this, basePath, sandboxPath).executeOnce(targetNames);

		Log.info("==== %s is done!", this);
	}

	@Override
	public Target getTarget(String name) throws NoSuchTargetException {
		return myTargetLoader.getTarget(name);
	}

}
