package org.msyu.reinforce;

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
import java.util.regex.Pattern;

public class YamlTargetLoader implements TargetRepository {

	public static final Pattern NAME_PATTERN = Pattern.compile("[-\\w]+");

	public static final String TARGET_DEPENDENCY_KEY = "depends on";

	public static final String FALLBACK_KEY = "fallback";

	private final Map<String, Map> myTargetDefinitions = new HashMap<>();

	private final TargetDefinitionStreamSource myTargetDefinitionStreamSource;

	public YamlTargetLoader(TargetDefinitionStreamSource targetDefinitionStreamSource) {
		this.myTargetDefinitionStreamSource = targetDefinitionStreamSource;
	}

	@Override
	public Target getTarget(String name) throws TargetDefinitionLoadingException, TargetConstructionException {
		return constructTarget(name, getTargetDefinition(name));
	}

	private Map getTargetDefinition(String targetName) throws TargetDefinitionLoadingException {
		if (!myTargetDefinitions.containsKey(targetName)) {
			myTargetDefinitions.put(targetName, loadDefinition(targetName));
		}
		return myTargetDefinitions.get(targetName);
	}

	private Map loadDefinition(String targetName) throws TargetDefinitionLoadingException {
		checkName(targetName);
		Log.verbose("Loading target from YAML definition");
		Object document = loadYamlDocument(targetName);
		if (!(document instanceof Map)) {
			throw new TargetDefinitionLoadingException("target definition document must be a map");
		}
		return (Map) document;
	}

	private static void checkName(String name) throws InvalidTargetNameException {
		if (name == null || !NAME_PATTERN.matcher(name).matches()) {
			throw new InvalidTargetNameException(name);
		}
	}

	private Object loadYamlDocument(String targetName) throws TargetDefinitionLoadingException {
		Log.debug("Requesting definition stream from source");
		try (InputStream targetDefStream = myTargetDefinitionStreamSource.getStreamForTarget(targetName + ".yaml")) {
			if (targetDefStream == null) {
				Log.verbose("Target definition not found; throwing");
				throw new TargetDefinitionNotFoundException(targetName);
			}
			Log.debug("Parsing YAML stream");
			return new Yaml(new SafeConstructor()).load(targetDefStream);
		} catch (IOException | YAMLException e) {
			Log.debug("Error while loading definition; throwing");
			throw new TargetDefinitionLoadingException(e);
		}
	}

	private Target constructTarget(String targetName, Map docMap) throws TargetConstructionException {
		Log.debug("Starting target object construction");
		String fallbackTargetName = getFallbackTargetName(docMap);
		try {
			Target target = createTargetObject(docMap, targetName);
			target.setDefinitionDocument(docMap);
			target.setDependencyTargetNames(
					docMap.containsKey(TARGET_DEPENDENCY_KEY) ?
							parseTargetDependencies(docMap.get(TARGET_DEPENDENCY_KEY)) :
							Collections.<String>emptySet()
			);
			Log.debug("Target object ready for initialization");
			return target;
		} catch (TargetConstructionException e) {
			e.setTargetName(targetName);
			throw new FallbackTargetConstructionException(fallbackTargetName, e);
		}
	}

	private String getFallbackTargetName(Map docMap) throws TargetConstructionException {
		if (!docMap.containsKey(FALLBACK_KEY)) {
			Log.debug("No fallback target specified");
			return null;
		}

		Object fallbackTargetName = docMap.get(FALLBACK_KEY);
		if (!(fallbackTargetName instanceof String)) {
			throw new TargetConstructionException("fallback setting must be a string");
		}
		return (String) fallbackTargetName;
	}

	private Target createTargetObject(Map docMap, String targetName) throws TargetConstructionException {
		Object typeObject = docMap.get("type");
		if (!(typeObject instanceof String)) {
			throw new TargetConstructionException("target must declare its type as a string");
		}
		return TargetFactories.createTargetObject((String) typeObject, targetName);
	}

	private Set<String> parseTargetDependencies(Object depList) throws InvalidTargetDependencyException {
		if (depList == null) {
			return Collections.emptySet();
		}
		Log.verbose("Parsing list of dependency targets");
		Set<String> dependencyNames = new LinkedHashSet<>();

		if (depList instanceof String) {
			dependencyNames.add((String) depList);
		} else if (depList instanceof List) {
			for (Object dependencyObject : (List) depList) {
				if (!(dependencyObject instanceof String)) {
					throw new InvalidTargetDependencyException("dependencies must be referenced by their string names");
				}
				dependencyNames.add((String) dependencyObject);
			}
		} else {
			throw new InvalidTargetDependencyException("dependency list is invalid");
		}

		Log.verbose("Finished parsing dependencies");
		return dependencyNames;
	}

}
