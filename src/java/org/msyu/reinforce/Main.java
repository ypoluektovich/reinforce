package org.msyu.reinforce;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class Main {

	protected static final int LOAD_FAILURE_EXIT_STATUS = 1;

	protected static final int EXECUTION_FAILURE_EXIT_STATUS = 2;

	public static void main(String[] args) {
		String targetSourceString = System.getProperty("reinforce.target.source", "src/build");
		Log.verbose("Loading targets from %s", targetSourceString);
		LinkedHashSet<String> targetNames = new LinkedHashSet<>(Arrays.asList(args));
		Log.verbose("Requested targets in order: %s", targetNames);

		TargetDefinitionStreamSource targetDefinitionStreamSource =
				new FileSystemTargetDefinitionStreamSource(Paths.get(targetSourceString));
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
			new Build(targetLoader).executeOnce(targetNames);
		} catch (NoSuchTargetException | BuildException e) {
			e.printStackTrace();
			System.exit(EXECUTION_FAILURE_EXIT_STATUS);
		}

		Log.info("==== Reinforce is done!");
	}

}
