package org.msyu.reinforce.target;

import org.msyu.reinforce.Build;
import org.msyu.reinforce.BuildException;
import org.msyu.reinforce.ExecutionException;
import org.msyu.reinforce.Reinforce;
import org.msyu.reinforce.interpretation.ReinterpretationException;
import org.msyu.reinforce.Target;
import org.msyu.reinforce.TargetInitializationException;
import org.msyu.reinforce.TargetInvocation;
import org.msyu.reinforce.interpretation.UnknownInterpretationException;
import org.msyu.reinforce.util.variables.VariableSubstitutionException;
import org.msyu.reinforce.util.variables.Variables;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ReinforceTarget extends Target {

	public static final String TARGET_DEFS_KEY = "target defs";

	public static final String BASE_PATH_KEY = "base path";

	public static final String TARGETS_KEY = "targets";

	public static final String SANDBOX_KEY = "sandbox path";

	public static final String VARIABLES_KEY = "variables";

	public static final String INHERIT_TARGETS_KEY = "inherit targets";

	public static final Pattern RESULT_OF_TARGET_PATTERN = Pattern.compile("^result of( all by regex)? (.++)$");

	private Path myTargetDefLocation;

	private Path myBasePath;

	private Path mySandboxPath;

	private Map<String, Object> myVariables;

	private Map<TargetInvocation, Target> myInheritedTargets;

	private final LinkedHashSet<TargetInvocation> myTargets = new LinkedHashSet<>();

	private Build myBuild;

	public ReinforceTarget(TargetInvocation invocation) {
		super(invocation);
	}

	@Override
	protected void initTarget(Map docMap) throws TargetInitializationException {
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

		initInheritedTargets(docMap);

		initTargetsToExecute(docMap);
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
			throw new TargetInitializationException("variable definitions must be specified as a name-value mapping");
		}
		for (Map.Entry<?, ?> variable : ((Map<?, ?>) variableDefs).entrySet()) {
			myVariables.put(getVariableName(variable), getVariableValue(variable));
		}
	}

	private static String getVariableName(Map.Entry<?, ?> variableDefEntry) throws TargetInitializationException {
		Object unexpandedNameObject = variableDefEntry.getKey();
		if (!(unexpandedNameObject instanceof String)) {
			throw new TargetInitializationException("new variable names must be specified as strings");
		}
		String unexpandedNameString = (String) unexpandedNameObject;
		Object expandedNameObject;
		try {
			expandedNameObject = Variables.expand(unexpandedNameString);
		} catch (VariableSubstitutionException e) {
			throw new TargetInitializationException(
					"error while expanding variables in new variable name definition '" + unexpandedNameString + "'",
					e
			);
		}
		if (!(expandedNameObject instanceof String)) {
			throw new TargetInitializationException("new variable names must be strings after variable expansion");
		}
		return (String) expandedNameObject;
	}

	private static Object getVariableValue(Map.Entry<?, ?> variable) throws TargetInitializationException {
		Object unexpandedValueObject = variable.getValue();
		if (!(unexpandedValueObject instanceof String)) {
			throw new TargetInitializationException("new variable values must be specified as strings");
		}
		String unexpandedValueString = (String) unexpandedValueObject;
		Object value;
		try {
			value = Variables.expand(unexpandedValueString);
		} catch (VariableSubstitutionException e) {
			throw new TargetInitializationException(
					"error while expanding variables in new variable value definition '" + unexpandedValueString + "'",
					e
			);
		}
		return value;
	}


	@SuppressWarnings("unchecked")
	private void initInheritedTargets(Map docMap) throws TargetInitializationException {
		if (!docMap.containsKey(INHERIT_TARGETS_KEY)) {
			return;
		}
		myInheritedTargets = new HashMap<>();
		Object inheritedTargetList = docMap.get(INHERIT_TARGETS_KEY);
		if (inheritedTargetList instanceof List) {
			for (Object inheritedTargetName : new HashSet((List) inheritedTargetList)) {
				addInheritedTarget(inheritedTargetName);
			}
		} else {
			addInheritedTarget(inheritedTargetList);
		}
	}

	private void addInheritedTarget(Object inheritedTargetDef) throws TargetInitializationException {
		if (!(inheritedTargetDef instanceof String)) {
			throw new TargetInitializationException(
					"value of '" + INHERIT_TARGETS_KEY + "' setting must be a string or a list of strings");
		}
		String expandedTargetDef;
		try {
			Object expandedTargetDefObject = Variables.expand((String) inheritedTargetDef);
			if (!(expandedTargetDefObject instanceof String)) {
				throw new TargetInitializationException("inherited target was variable-expanded to a non-string");
			}
			expandedTargetDef = (String) expandedTargetDefObject;
		} catch (VariableSubstitutionException e) {
			throw new TargetInitializationException("error while expanding variables in inherited target spec", e);
		}
		TargetInvocation invocation = TargetInvocation.parse(expandedTargetDef);
		if (!Build.getCurrent().isTargetExecuted(invocation)) {
			throw new TargetInitializationException("target '" + expandedTargetDef + "' is not available for inheritance");
		}
		myInheritedTargets.put(invocation, Build.getCurrent().getExecutedTarget(invocation));
	}

	private void initTargetsToExecute(Map docMap) throws TargetInitializationException {
		myTargets.clear();
		Object targets = docMap.get(TARGETS_KEY);
		if (targets instanceof List) {
			List targetList = (List) targets;
			for (int i = 0; i < targetList.size(); i++) {
				addTargetToExecute(targetList.get(i), i);
			}
		} else {
			addTargetToExecute(targets, 0);
		}
	}

	private void addTargetToExecute(Object targetSpec, int index) throws TargetInitializationException {
		if (!(targetSpec instanceof String)) {
			throw new TargetInitializationException("invalid target invocation spec in position " + index + ": must be a string");
		}
		String expandedTargetSpec;
		try {
			Object expandedTargetSpecObject = Variables.expand((String) targetSpec);
			if (!(expandedTargetSpecObject instanceof String)) {
				throw new TargetInitializationException("target invocation was variable-expanded to a non-string");
			}
			expandedTargetSpec = (String) expandedTargetSpecObject;
		} catch (VariableSubstitutionException e) {
			throw new TargetInitializationException("error while expanding variables in target invocation spec in position " + index);
		}
		TargetInvocation invocation = TargetInvocation.parse(expandedTargetSpec);
		if (invocation == null) {
			throw new TargetInitializationException("invalid target invocation spec in position " + index + ": invalid format");
		}
		myTargets.add(invocation);
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
			boolean simpleMatch = matcher.group(1) == null;
			String targetName = matcher.group(2);
			if (simpleMatch) {
				Target target = myBuild.getExecutedTarget(TargetInvocation.parse(targetName));
				if (target != null) {
					return target;
				}
				throw new UnknownTargetInterpretationException(simpleMatch, targetName);
			} else {
				Pattern targetNamePattern;
				try {
					targetNamePattern = Pattern.compile(targetName);
				} catch (PatternSyntaxException e) {
					throw new InvalidRegexInterpretationException(targetName);
				}
				List<Target> targets = new ArrayList<>();
				for (TargetInvocation invocation : myBuild.getExecutedTargets()) {
					if (targetNamePattern.matcher(invocation.toString()).matches()) {
						targets.add(myBuild.getExecutedTarget(invocation));
					}
				}
				return targets;
			}
		} else {
			return super.reinterpret(interpretationSpec);
		}
	}

	public final class InvalidRegexInterpretationException extends UnknownInterpretationException {

		private InvalidRegexInterpretationException(String targetNameRegex) {
			super("invalid target name regex: " + targetNameRegex);
		}

	}

	public final class UnknownTargetInterpretationException extends UnknownInterpretationException {

		private UnknownTargetInterpretationException(boolean simpleMatch, String targetName) {
			super(
					(simpleMatch ?
							("target '" + targetName + "' has not") :
							("no target that matches '" + targetName + "' has")
					) +
					" been executed by " + ReinforceTarget.this.myBuild.getReinforce());
		}

		public final ReinforceTarget getReinforceTarget() {
			return ReinforceTarget.this;
		}

	}

}
