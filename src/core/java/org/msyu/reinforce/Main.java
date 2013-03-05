package org.msyu.reinforce;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

public class Main {

	protected static final int ERROR_EXIT_STATUS = 1;

	public static void main(String[] args) {
		Reinforce reinforce = getReinforce();

		Path basePath;
		try {
			basePath = Paths.get(System.getProperty("reinforce.build.basePath", "."));
		} catch (InvalidPathException e) {
			Log.error("Value specified as base path is not a valid path");
			System.exit(ERROR_EXIT_STATUS);
			return;
		}

		Path sandboxPath;
		try {
			sandboxPath = Paths.get(System.getProperty("reinforce.build.sandboxPath", "build"));
		} catch (InvalidPathException e) {
			Log.error("Value specified as sandbox path is not a valid path");
			System.exit(ERROR_EXIT_STATUS);
			return;
		}

		LinkedHashSet<String> targetNames = new LinkedHashSet<>(Arrays.asList(args));
		List<TargetInvocation> invocations = TargetInvocation.parse(targetNames);
		if (invocations.contains(null)) {
			Log.error("Invalid target invocation spec at position %d", invocations.indexOf(null));
			System.exit(ERROR_EXIT_STATUS);
			return;
		}

		try {
			reinforce.executeNewBuild(basePath, sandboxPath, null, null, invocations);
		} catch (BuildException e) {
			reportBuildException(e);
			System.exit(ERROR_EXIT_STATUS);
		}
	}

	private static Reinforce getReinforce() {
		Path targetDefLocation;
		try {
			targetDefLocation = Paths.get(System.getProperty("reinforce.targetDef.location", "reinforce"));
		} catch (InvalidPathException e) {
			Log.error("Value specified as target definition location is not a valid path");
			System.exit(ERROR_EXIT_STATUS);
			return null;
		}
		return new Reinforce(targetDefLocation);
	}

	private static void reportBuildException(BuildException e) {
		if (!Log.getLevel().acceptMessageOfLevel(Log.Level.DEBUG)) {
			overwriteStackTraces(e);
		}
		e.printStackTrace();
	}

	private static void overwriteStackTraces(Throwable throwable) {
		if (throwable == null) {
			return;
		}
		throwable.setStackTrace(new StackTraceElement[0]);
		overwriteStackTraces(throwable.getCause());
		for (Throwable suppressed : throwable.getSuppressed()) {
			overwriteStackTraces(suppressed);
		}
	}

}
