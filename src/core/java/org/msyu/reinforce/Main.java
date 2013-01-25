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
		Reinforce reinforce = new Reinforce();

		try {
			reinforce.setTargetDefLocation(Paths.get(System.getProperty("reinforce.targetDef.location", "reinforce")));
		} catch (InvalidPathException e) {
			Log.error("Value specified as target definition location is not a valid path");
			System.exit(LOAD_FAILURE_EXIT_STATUS);
			return;
		}

		Path basePath;
		try {
			basePath = Paths.get(System.getProperty("reinforce.build.basePath", "."));
		} catch (InvalidPathException e) {
			Log.error("Value specified as base path is not a valid path");
			System.exit(LOAD_FAILURE_EXIT_STATUS);
			return;
		}

		Path sandboxPath;
		try {
			sandboxPath = Paths.get(System.getProperty("reinforce.build.sandboxPath", "build"));
		} catch (InvalidPathException e) {
			Log.error("Value specified as sandbox path is not a valid path");
			System.exit(LOAD_FAILURE_EXIT_STATUS);
			return;
		}

		try {
			LinkedHashSet<String> targetNames = new LinkedHashSet<>(Arrays.asList(args));
			reinforce.executeNewBuild(basePath, sandboxPath, targetNames);
		} catch (InvalidTargetNameException | TargetLoadingException e) {
			e.printStackTrace();
			System.exit(LOAD_FAILURE_EXIT_STATUS);
		} catch (BuildException e) {
			e.printStackTrace();
			System.exit(EXECUTION_FAILURE_EXIT_STATUS);
		}
	}

}
