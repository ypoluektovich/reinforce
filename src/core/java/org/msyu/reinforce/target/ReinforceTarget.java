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

	private Path myTargetDefLocation;

	private Path myBasePath;

	private final LinkedHashSet<String> myTargets = new LinkedHashSet<>();

	public ReinforceTarget(String name) {
		super(name);
	}

	@Override
	protected void initTarget(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		if (docMap.containsKey(TARGET_DEFS_KEY)) {
			Object targetDefLocation = docMap.get(TARGET_DEFS_KEY);
			if (!(targetDefLocation instanceof String)) {
				throw new TargetInitializationException("'" + TARGET_DEFS_KEY + "' must be a string");
			}
			try {
				myTargetDefLocation = Paths.get((String) targetDefLocation);
			} catch (InvalidPathException e) {
				throw new TargetInitializationException("value of '" + TARGET_DEFS_KEY + "' is not a valid path", e);
			}
		} else {
			myTargetDefLocation = null;
		}

		if (docMap.containsKey(BASE_PATH_KEY)) {
			Object basePath = docMap.get(BASE_PATH_KEY);
			if (!(basePath instanceof String)) {
				throw new TargetInitializationException("'" + BASE_PATH_KEY + "' must be a string");
			}
			try {
				myBasePath = Paths.get((String) basePath);
			} catch (InvalidPathException e) {
				throw new TargetInitializationException("value of '" + BASE_PATH_KEY + "' is not a valid path", e);
			}
		} else {
			myBasePath = null;
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
					myTargets
			);
		} catch (InvalidTargetNameException | TargetLoadingException | BuildException e) {
			throw new BuildException("Reinforce invocation ended with an exception", e);
		}
	}

}
