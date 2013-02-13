package org.msyu.reinforce.target;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.BuildException;
import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Reinforce;
import org.msyu.reinforce.ReinterpretationException;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReinforceTarget extends Target {

	public static final String TARGET_DEFS_KEY = "target defs";

	public static final String BASE_PATH_KEY = "base path";

	public static final String TARGETS_KEY = "targets";

	public static final String SANDBOX_KEY = "sandbox path";

	public static final Pattern RESULT_OF_TARGET_PATTERN = Pattern.compile("result of (.++)");

	private Path myTargetDefLocation;

	private Path myBasePath;

	private Path mySandboxPath;

	private final LinkedHashSet<String> myTargets = new LinkedHashSet<>();

	private Build myBuild;

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
	public void run() throws ExecutionException {
		Reinforce reinforce = new Reinforce(
				myTargetDefLocation == null ?
						Build.getCurrent().getReinforce().getTargetDefLocation() :
						Build.getCurrent().getBasePath().resolve(myTargetDefLocation)
		);

		try {
			myBuild = reinforce.executeNewBuild(
					myBasePath == null ?
							Build.getCurrent().getBasePath() :
							Build.getCurrent().getBasePath().resolve(myBasePath),
					mySandboxPath == null ? Paths.get("build") : mySandboxPath,
					myTargets
			);
		} catch (BuildException e) {
			throw new ExecutionException("Reinforce invocation ended with an exception", e);
		}
	}

	@Override
	public Object reinterpret(String interpretationSpec) throws ReinterpretationException {
		Matcher matcher = RESULT_OF_TARGET_PATTERN.matcher(interpretationSpec);
		if (matcher.matches()) {
			String targetName = matcher.group(1);
			Target target = myBuild.getExecutedTarget(targetName);
			if (target != null) {
				return target;
			}
			throw new ReinterpretationException("target '" + targetName + "' has not been executed");
		} else {
			return super.reinterpret(interpretationSpec);
		}
	}
}
