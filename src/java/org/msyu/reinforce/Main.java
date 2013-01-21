package org.msyu.reinforce;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class Main {

	protected static final int LOAD_FAILURE_EXIT_STATUS = 1;

	protected static final int EXECUTION_FAILURE_EXIT_STATUS = 2;

	public static void main(String[] args) {
		Path basePath;
		try {
			String basePathString = System.getProperty("reinforce.build.basePath", ".");
			basePath = Paths.get(basePathString);
			Log.verbose("Build base path is %s", basePathString);
		} catch (InvalidPathException e) {
			Log.error("Value specified as base path is not a valid path");
			System.exit(LOAD_FAILURE_EXIT_STATUS);
			return;
		}
		Path targetDefLocation;
		try {
			String targetSourceString = System.getProperty("reinforce.targetDef.location", "src/build");
			targetDefLocation = Paths.get(targetSourceString);
			Log.verbose("Loading targets from %s", targetSourceString);
		} catch (InvalidPathException e) {
			Log.error("Value specified as target definition location is not a valid path");
			System.exit(LOAD_FAILURE_EXIT_STATUS);
			return;
		}
		LinkedHashSet<String> targetNames = new LinkedHashSet<>(Arrays.asList(args));
		Log.verbose("Requested targets in order: %s", targetNames);

		TargetDefinitionStreamSource targetDefinitionStreamSource =
				new FileSystemTargetDefinitionStreamSource(targetDefLocation);
		YamlTargetLoader targetLoader = new YamlTargetLoader(targetDefinitionStreamSource);

		Log.info("==== Loading targets");
		try {
			targetLoader.load(targetNames);
		} catch (InvalidTargetNameException | TargetLoadingException e) {
			e.printStackTrace();
			System.exit(LOAD_FAILURE_EXIT_STATUS);
		}

		Log.info("==== Executing targets");
		try {
			new Build(
					targetLoader,
					basePath
			).executeOnce(targetNames);
		} catch (NoSuchTargetException | BuildException e) {
			e.printStackTrace();
			System.exit(EXECUTION_FAILURE_EXIT_STATUS);
		}

		Log.info("==== Reinforce is done!");
	}

}
