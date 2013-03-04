package org.msyu.reinforce;

import org.msyu.reinforce.util.variables.VariableSource;
import org.msyu.reinforce.util.variables.VariableSubstitutionException;
import org.msyu.reinforce.util.variables.Variables;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class YamlTargetLoader implements TargetRepository {

	public static final String TARGET_DEPENDENCY_KEY = "depends on";

	public static final String FALLBACK_KEY = "fallback";

	private static final ThreadLocal<VariableSource> ourVariableSource = new ThreadLocal<>();

	private final Map<String, Map> myTargetDefinitions = new HashMap<>();

	private final TargetDefinitionStreamSource myTargetDefinitionStreamSource;

	public YamlTargetLoader(TargetDefinitionStreamSource targetDefinitionStreamSource) {
		this.myTargetDefinitionStreamSource = targetDefinitionStreamSource;
	}

	@Override
	public Target getTarget(TargetInvocation invocation) throws TargetNotFoundException, TargetLoadingException {
		VariableSource oldVariableSource = ourVariableSource.get();
		Build currentBuild = Build.getCurrent();
		ourVariableSource.set(Variables.sourceFromChain(
				Variables.sourceFromMap(invocation.getParameters()),
				(currentBuild == null) ? null : currentBuild.getBuildVariables()
		));
		try {
			return constructTarget(invocation, getTargetDefinition(invocation.getTargetName()));
		} finally {
			ourVariableSource.set(oldVariableSource);
		}
	}

	private Map getTargetDefinition(String targetName) throws TargetNotFoundException, TargetDefinitionLoadingException {
		if (!myTargetDefinitions.containsKey(targetName)) {
			myTargetDefinitions.put(targetName, loadDefinition(targetName));
		}
		return myTargetDefinitions.get(targetName);
	}

	private Map loadDefinition(String targetName) throws TargetNotFoundException, TargetDefinitionLoadingException {
		Log.verbose("Loading target from YAML definition");
		Object document = loadYamlDocument(targetName);
		if (!(document instanceof Map)) {
			throw new TargetDefinitionLoadingException("target definition document must be a map");
		}
		return (Map) document;
	}

	private Object loadYamlDocument(String targetName) throws TargetNotFoundException, TargetDefinitionLoadingException {
		Log.debug("Requesting definition stream from source");
		try (InputStream targetDefStream = myTargetDefinitionStreamSource.getStreamForTarget(targetName + ".yaml")) {
			if (targetDefStream == null) {
				Log.verbose("Target definition not found; throwing");
				throw new TargetNotFoundException(targetName);
			}
			Log.debug("Parsing YAML stream");
			return new Yaml(new SafeConstructor()).load(targetDefStream);
		} catch (IOException | YAMLException e) {
			Log.debug("Error while loading definition; throwing");
			throw new TargetDefinitionLoadingException(e);
		}
	}

	private Target constructTarget(TargetInvocation invocation, Map docMap) throws TargetConstructionException {
		Log.debug("Starting target object construction");
		TargetInvocation fallbackTargetInvocation = null;
		try {
			fallbackTargetInvocation = getFallbackTargetInvocation(docMap);
			Target target = createTargetObject(docMap, invocation);
			target.setDefinitionDocument(docMap);
			if (docMap.containsKey(TARGET_DEPENDENCY_KEY)) {
				target.setDependencyTargets(parseTargetDependencies(docMap.get(TARGET_DEPENDENCY_KEY)));
			}
			Log.debug("Target object ready for initialization");
			return target;
		} catch (TargetConstructionException e) {
			e.setInvocation(invocation);
			if (fallbackTargetInvocation != null) {
				throw new FallbackTargetConstructionException(fallbackTargetInvocation, e);
			} else {
				throw e;
			}
		}
	}

	private TargetInvocation getFallbackTargetInvocation(Map docMap) throws TargetConstructionException {
		if (!docMap.containsKey(FALLBACK_KEY)) {
			Log.debug("No fallback target specified");
			return null;
		}
		Object fallbackTargetSetting = docMap.get(FALLBACK_KEY);
		if (!(fallbackTargetSetting instanceof String)) {
			throw new TargetConstructionException("fallback setting must be a string");
		}
		String fallbackTargetSpec;
		try {
			fallbackTargetSpec = Variables.expand((String) fallbackTargetSetting, ourVariableSource.get());
		} catch (VariableSubstitutionException e) {
			throw new TargetConstructionException("error while expanding variables in fallback setting", e);
		}
		TargetInvocation invocation = TargetInvocation.parse(fallbackTargetSpec);
		if (invocation == null) {
			throw new TargetConstructionException("fallback setting must be a correct target invocation");
		}
		return invocation;
	}

	private Target createTargetObject(Map docMap, TargetInvocation invocation) throws TargetConstructionException {
		Object typeObject = docMap.get("type");
		if (!(typeObject instanceof String)) {
			throw new TargetConstructionException("target must declare its type as a string");
		}
		return TargetFactories.createTargetObject((String) typeObject, invocation);
	}

	private Set<TargetInvocation> parseTargetDependencies(Object depList) throws InvalidTargetDependencyException {
		if (depList == null) {
			return Collections.emptySet();
		}
		Log.verbose("Parsing list of dependency targets");
		Set<TargetInvocation> invocations = new LinkedHashSet<>();
		if (depList instanceof List) {
			for (Object dependencyObject : (List) depList) {
				invocations.add(expandAndParseInvocation(dependencyObject));
			}
		} else {
			invocations.add(expandAndParseInvocation(depList));
		}
		Log.verbose("Finished parsing dependencies");
		return invocations;
	}

	private TargetInvocation expandAndParseInvocation(Object dependencyObject) throws InvalidTargetDependencyException {
		if (!(dependencyObject instanceof String)) {
			throw new InvalidTargetDependencyException("dependencies must be listed as strings");
		}
		String expandedDependency = null;
		try {
			expandedDependency = Variables.expand((String) dependencyObject, ourVariableSource.get());
		} catch (VariableSubstitutionException e) {
			throw new InvalidTargetDependencyException("error while expanding variables in dependency spec", e);
		}
		TargetInvocation invocation = TargetInvocation.parse(expandedDependency);
		if (invocation == null) {
			throw new InvalidTargetDependencyException("not a target: " + dependencyObject);
		}
		return invocation;
	}

}
