package org.msyu.reinforce.target;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.BuildException;
import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Reinforce;
import org.msyu.reinforce.ReinterpretationException;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.util.variables.VariableSubstitutionException;
import org.msyu.reinforce.util.variables.Variables;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

	public static final String VARIABLES_KEY = "variables";

	public static final String INHERIT_TARGETS_KEY = "inherit targets";

	public static final Pattern RESULT_OF_TARGET_PATTERN = Pattern.compile("result of (.++)");

	private Path myTargetDefLocation;

	private Path myBasePath;

	private Path mySandboxPath;

	private Map<String, String> myVariables;

	private Map<String, Target> myInheritedTargets;

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

		initVariables(docMap);

		initInheritedTargets(docMap, dependencyTargetByName);

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

	private void initVariables(Map docMap) throws TargetInitializationException {
		if (!docMap.containsKey(VARIABLES_KEY)) {
			return;
		}
		myVariables = new HashMap<>();
		Object variableDefs = docMap.get(VARIABLES_KEY);
		if (!(variableDefs instanceof Map)) {
			throw newVariableInitializationException();
		}
		for (Map.Entry<?, ?> variable : ((Map<?, ?>) variableDefs).entrySet()) {
			if (variable.getKey() instanceof String && variable.getValue() instanceof String) {
				try {
					myVariables.put(
							Variables.expand((String) variable.getKey()),
							Variables.expand((String) variable.getValue())
					);
				} catch (VariableSubstitutionException e) {
					throw new TargetInitializationException(
							"error while expanding variables in '" + VARIABLES_KEY + "' setting",
							e
					);
				}
			} else {
				throw newVariableInitializationException();
			}
		}
	}

	private TargetInitializationException newVariableInitializationException() {
		return new TargetInitializationException("variable definitions must be specified as a string-string mapping");
	}


	@SuppressWarnings("unchecked")
	private void initInheritedTargets(Map docMap, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		if (!docMap.containsKey(INHERIT_TARGETS_KEY)) {
			return;
		}
		myInheritedTargets = new HashMap<>();
		Object inheritedTargetList = docMap.get(INHERIT_TARGETS_KEY);
		if (inheritedTargetList instanceof List) {
			for (Object inheritedTargetName : new HashSet((List) inheritedTargetList)) {
				addInheritedTarget(inheritedTargetName, dependencyTargetByName);
			}
		} else {
			addInheritedTarget(inheritedTargetList, dependencyTargetByName);
		}
	}

	private void addInheritedTarget(Object inheritedTargetDef, Map<String, Target> dependencyTargetByName) throws TargetInitializationException {
		if (!(inheritedTargetDef instanceof String)) {
			throw new TargetInitializationException(
					"value of '" + INHERIT_TARGETS_KEY + "' setting must be a string or a list of strings");
		}
		if (!dependencyTargetByName.containsKey(inheritedTargetDef)) {
			throw new TargetInitializationException(
					"target with name '" + inheritedTargetDef + "' is not available as a dependency for inheritance");
		}
		String inheritedTargetName = null;
		try {
			inheritedTargetName = Variables.expand((String) inheritedTargetDef);
		} catch (VariableSubstitutionException e) {
			throw new TargetInitializationException("error while expanding inherited target name", e);
		}
		myInheritedTargets.put(inheritedTargetName, dependencyTargetByName.get(inheritedTargetName));
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
					myVariables,
					myInheritedTargets == null ? null : Collections.unmodifiableMap(myInheritedTargets),
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
