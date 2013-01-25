package org.msyu.reinforce.target;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.BuildException;
import org.msyu.reinforce.InvalidTargetNameException;
import org.msyu.reinforce.Reinforce;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.TargetLoadingException;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class ReinforceTarget extends Target {

	public static final String TARGET_DEFS_KEY = "target defs";

	public static final String BASE_PATH_KEY = "base path";

	public static final String TARGETS_KEY = "targets";

	public static final String SANDBOX_KEY = "sandbox path";

	private Path myTargetDefLocation;

	private Path myBasePath;

	private Path mySandboxPath;

	private final LinkedHashSet<String> myTargets = new LinkedHashSet<>();

	public ReinforceTarget(String name) {
		super(name);
	}

	@Override
	protected void initTarget(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		if (docMap.containsKey(TARGET_DEFS_KEY)) {
			myTargetDefLocation = getStringAsPath(docMap, TARGET_DEFS_KEY);
		}

		if (docMap.containsKey(BASE_PATH_KEY)) {
			myBasePath = getStringAsPath(docMap, BASE_PATH_KEY);
		}

		if (docMap.containsKey(SANDBOX_KEY)) {
			mySandboxPath = getStringAsPath(docMap, SANDBOX_KEY);
		}

		if (!docMap.containsKey(TARGETS_KEY)) {
			throw new TargetInitializationException("missing required parameter: " + TARGETS_KEY);
		}

		myTargets.clear();
		Object targets = docMap.get(TARGETS_KEY);
		if (targets instanceof String) {
			myTargets.add((String) targets);
		} else if (targets instanceof List) {
			List targetList = (List) targets;
			for (int i = 0; i < targetList.size(); i++) {
				Object target = targetList.get(i);
				if (target instanceof String) {
					myTargets.add((String) target);
				} else {
					throw new TargetInitializationException("invalid target name on position " + i + ": must be a string");
				}
			}
		}
	}

	private Path getStringAsPath(Map docMap, String key) throws TargetInitializationException {
		Object basePath = docMap.get(key);
		if (!(basePath instanceof String)) {
			throw new TargetInitializationException(key + " must be a string");
		}
		try {
			return Paths.get((String) basePath);
		} catch (InvalidPathException e) {
			throw new TargetInitializationException("value of '" + key + "' is not a valid path", e);
		}
	}

	@Override
	public void run() throws BuildException {
		Reinforce reinforce = new Reinforce();
		reinforce.setTargetDefLocation(
				myTargetDefLocation == null ?
						Build.getCurrent().getReinforce().getTargetDefLocation() :
						Build.getCurrent().getBasePath().resolve(myTargetDefLocation)
		);
		try {
			reinforce.executeNewBuild(
					myBasePath == null ?
							Build.getCurrent().getBasePath() :
							Build.getCurrent().getBasePath().resolve(myBasePath),
					Build.getCurrent().getBasePath().resolve(
							mySandboxPath == null ? Paths.get("build") : mySandboxPath
					),
					myTargets
			);
		} catch (InvalidTargetNameException | TargetLoadingException | BuildException e) {
			throw new BuildException("Reinforce invocation ended with an exception", e);
		}
	}

}
